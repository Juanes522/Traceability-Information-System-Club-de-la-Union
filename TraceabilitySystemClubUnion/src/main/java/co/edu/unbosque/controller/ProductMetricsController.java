package co.edu.unbosque.controller;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.model.PersonPartner;
import co.edu.unbosque.service.PersonPartnerService;
import co.edu.unbosque.service.ProductMetricsService;

@RestController
@RequestMapping("/metrics/products")
/**
 * Endpoints de analítica de productos: rankings, mezcla por categoría y cruce con ambientes.
 *
 * <p>Toda la información de producto del sistema proviene de las líneas de detalle de los consumos, de modo que este
 * controlador es la única ventana a «qué se consume», frente al resto de las métricas, que responden «cuánto se factura».
 *
 * <p>Como {@link PartnerMetricsController}, expone la vista individual por dos vías —{@code /partner/me} para el propio
 * socio y {@code /partner/{identification}} para los roles privilegiados— con la misma dependencia de la precedencia de
 * segmentos literales del emparejador de rutas.
 *
 * <p>Comparte la política de ventana por defecto de 30 días y tope de 366, con los mismos métodos auxiliares duplicados.
 *
 * @see co.edu.unbosque.service.ProductMetricsService
 */
public class ProductMetricsController {

	private static final long MAX_RANGE_DAYS = 366;

	private final ProductMetricsService metrics;
	private final PersonPartnerService partnerService;

	public ProductMetricsController(ProductMetricsService metrics, PersonPartnerService partnerService) {
		this.metrics = metrics;
		this.partnerService = partnerService;
	}

	@PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
	/**
	 * Devuelve el ranking de productos del periodo.
	 *
	 * <p>Admite dos ejes de configuración: el criterio de orden y el filtro por ambiente.
	 *
	 * <p>Dos advertencias sobre los parámetros. Cualquier valor de {@code sort} distinto de {@code quantity} se interpreta
	 * <strong>silenciosamente</strong> como orden por ingresos, sin error. Y {@code limit} <strong>no tiene cota
	 * superior</strong>: el recorte ocurre en memoria después de que la base haya materializado todos los grupos, de modo
	 * que un valor alto no abarata la consulta.
	 *
	 * @param from        inicio del periodo; si se omite, 30 días atrás
	 * @param to          fin del periodo; si se omite, ahora
	 * @param environment nombre exacto del ambiente, o vacío para no filtrar
	 * @param sort        {@code quantity} para ordenar por unidades; cualquier otro valor ordena por ingresos
	 * @param limit       número máximo de productos a devolver
	 * @return {@code 200} con el ranking, o {@code 400} si el rango es inválido
	 */
	@GetMapping("/top")
	public ResponseEntity<?> top(
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
			@RequestParam(required = false) String environment,
			@RequestParam(defaultValue = "revenue") String sort,
			@RequestParam(defaultValue = "20") int limit) {
		LocalDateTime[] r = resolve(from, to);
		if (r == null) {
			return rangeError();
		}
		return ResponseEntity.ok(metrics.top(r[0], r[1], environment, sort, limit));
	}

	@PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
	/**
	 * Devuelve la distribución de ingresos por categoría y subcategoría, con su peso porcentual.
	 *
	 * <p>Responde a la composición de la venta: qué peso tiene cada familia de producto. No se recorta, de modo que
	 * devuelve todas las combinaciones presentes en el periodo.
	 *
	 * @param from inicio del periodo; si se omite, 30 días atrás
	 * @param to   fin del periodo; si se omite, ahora
	 * @return {@code 200} con la mezcla ordenada por ingresos, o {@code 400} si el rango es inválido
	 */
	@GetMapping("/category-mix")
	public ResponseEntity<?> categoryMix(
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
		LocalDateTime[] r = resolve(from, to);
		if (r == null) {
			return rangeError();
		}
		return ResponseEntity.ok(metrics.categoryMix(r[0], r[1]));
	}

	@PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
	/**
	 * Devuelve el cruce de ambiente con categoría de producto.
	 *
	 * <p>Permite distinguir un bar donde predomina la bebida de un restaurante donde predomina la comida: cruza una
	 * dimensión del consumo con una del producto, algo que ninguna otra métrica hace.
	 *
	 * @param from inicio del periodo; si se omite, 30 días atrás
	 * @param to   fin del periodo; si se omite, ahora
	 * @return {@code 200} con un elemento por par ambiente/categoría, o {@code 400} si el rango es inválido
	 */
	@GetMapping("/by-environment")
	public ResponseEntity<?> byEnvironment(
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
		LocalDateTime[] r = resolve(from, to);
		if (r == null) {
			return rangeError();
		}
		return ResponseEntity.ok(metrics.byEnvironmentCategory(r[0], r[1]));
	}

	@PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
	/**
	 * Devuelve el ranking de productos de un socio indicado por su identificación.
	 *
	 * <p>Variante privilegiada de {@code /partner/me}: acepta un identificador, y lo que impide el acceso indebido es el
	 * {@code @PreAuthorize} de rol.
	 *
	 * @param identification cédula del socio a consultar
	 * @param from           inicio del periodo; si se omite, 30 días atrás
	 * @param to             fin del periodo; si se omite, ahora
	 * @param limit          número máximo de productos
	 * @return {@code 200} con el ranking del socio; {@code 400} si el rango es inválido; {@code 404} si el socio no existe
	 */
	@GetMapping("/partner/{identification}")
	public ResponseEntity<?> partner(
			@PathVariable String identification,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
			@RequestParam(defaultValue = "20") int limit) {
		return partnerTop(identification, from, to, limit);
	}

	/**
	 * Devuelve el ranking de productos del propio socio.
	 *
	 * <p>Patrón {@code /me}: la identidad se resuelve del contexto de seguridad y no se acepta ningún identificador, de modo
	 * que el acceso es seguro sin guarda de rol.
	 *
	 * @param from  inicio del periodo; si se omite, 30 días atrás
	 * @param to    fin del periodo; si se omite, ahora
	 * @param limit número máximo de productos
	 * @return {@code 200} con el ranking propio; {@code 400} si el rango es inválido; {@code 401} sin sesión; {@code 404} si
	 *         el socio no existe
	 */
	@GetMapping("/partner/me")
	public ResponseEntity<?> me(
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
			@RequestParam(defaultValue = "20") int limit) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() instanceof String) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}
		String identification = ((UserDetails) auth.getPrincipal()).getUsername();
		return partnerTop(identification, from, to, limit);
	}

	private ResponseEntity<?> partnerTop(String identification, LocalDateTime from, LocalDateTime to, int limit) {
		LocalDateTime[] r = resolve(from, to);
		if (r == null) {
			return rangeError();
		}
		PersonPartner p = partnerService.getByIdentification(identification);
		if (p == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		return ResponseEntity.ok(metrics.topByPartner(p.getPersonId(), r[0], r[1], limit));
	}

	/**
	 * Normaliza y valida la ventana temporal solicitada, con los mismos criterios que los demás controladores de métricas:
	 * fin igual a ahora, inicio 30 días antes, tope de 366 días.
	 *
	 * @param from inicio solicitado, o {@code null}
	 * @param to   fin solicitado, o {@code null}
	 * @return un arreglo con el inicio y el fin normalizados, o {@code null} si el rango no es aceptable
	 */
	private LocalDateTime[] resolve(LocalDateTime from, LocalDateTime to) {
		LocalDateTime effectiveTo = to != null ? to : LocalDateTime.now();
		LocalDateTime effectiveFrom = from != null ? from : effectiveTo.minusDays(30);
		if (effectiveFrom.isAfter(effectiveTo)
				|| ChronoUnit.DAYS.between(effectiveFrom, effectiveTo) > MAX_RANGE_DAYS) {
			return null;
		}
		return new LocalDateTime[] { effectiveFrom, effectiveTo };
	}

	/**
	 * Construye la respuesta de rango inválido.
	 *
	 * @return {@code 400} con un cuerpo JSON que contiene el mensaje de error
	 */
	private ResponseEntity<?> rangeError() {
		return ResponseEntity.badRequest().body(Map.of("message", "El rango no puede superar 1 año"));
	}
}
