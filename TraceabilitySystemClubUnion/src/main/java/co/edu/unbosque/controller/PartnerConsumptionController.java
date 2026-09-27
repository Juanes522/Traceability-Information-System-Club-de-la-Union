package co.edu.unbosque.controller;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import co.edu.unbosque.dto.ConsumptionCreateRequest;
import co.edu.unbosque.model.PartnerConsumption;
import co.edu.unbosque.model.PersonPartner;
import co.edu.unbosque.service.PartnerConsumptionService;
import co.edu.unbosque.service.PersonPartnerService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/partnerconsumption")
/**
 * Endpoints de registro y consulta de consumos.
 *
 * <p>Contiene la <strong>operación de escritura central del sistema</strong> —el registro de un cargo— y dos consultas.
 *
 * <p>Es también el único controlador que implementa una <strong>guarda explícita de propiedad</strong>
 * ({@link #canAccessPartner(Long)}) para un endpoint que sí acepta un identificador del cliente. El resto del sistema evita
 * ese problema con el patrón {@code /me}, que no acepta identificadores.
 *
 * <h2>Advertencia: el registro de consumos no tiene control de autorización</h2>
 *
 * <p>{@link #registerConsumption} <strong>carece de {@code @PreAuthorize}</strong> y toma el identificador del socio del
 * cuerpo de la petición. Cualquier principal autenticado —incluido un socio— puede crear un cargo económico contra la
 * cuenta de cualquier otro. Es el único endpoint de escritura del sistema sin guarda de rol ni de propiedad.
 *
 * <p>Atenuante circunstancial, no de diseño: ninguna pantalla lo invoca, de modo que explotarlo requiere llamar a la API
 * directamente.
 *
 * @see co.edu.unbosque.service.PartnerConsumptionService
 */
public class PartnerConsumptionController {

	@Autowired
	private PartnerConsumptionService consumptionServ;

	@Autowired
	private PersonPartnerService personPartnerService;

	public PartnerConsumptionController() {
	}

	@PostMapping(path = "/registerconsumption")
	/**
	 * Registra un consumo y desencadena la notificación al socio.
	 *
	 * <p>Es el punto de entrada de la trazabilidad. Presupone una integración con el punto de venta que no forma parte de
	 * este repositorio: <strong>ninguna pantalla del frontend lo invoca</strong>.
	 *
	 * <p><strong>No tiene control de autorización</strong> y el identificador del socio se toma del cuerpo. Véase la
	 * advertencia en la documentación de la clase.
	 *
	 * <p>Dos particularidades del manejo de errores que conviene conocer:
	 *
	 * <ul>
	 *   <li>Captura <strong>toda</strong> excepción y responde 400. Por eso «socio no encontrado» se manifiesta como 400 y
	 *       no como 404, que sería lo esperable.</li>
	 *   <li>La rama que devolvería 404 ante un resultado nulo es <strong>inalcanzable</strong>: el servicio o devuelve una
	 *       entidad o lanza excepción.</li>
	 * </ul>
	 *
	 * <p>El consumo devuelto se serializa <strong>sin sus líneas de detalle</strong>, pese a que se acaban de crear, porque
	 * esa colección está excluida de la serialización.
	 *
	 * @param req datos del consumo y sus líneas, validados por Bean Validation
	 * @return {@code 201} con el consumo persistido; {@code 400} ante cualquier error, incluido un socio inexistente
	 */
	public ResponseEntity<PartnerConsumption> registerConsumption(@Valid @RequestBody ConsumptionCreateRequest req) {
		try {
			PartnerConsumption consumption = consumptionServ.register(req);
			if (consumption == null) {
				return new ResponseEntity<>(HttpStatus.NOT_FOUND);
			}
			return new ResponseEntity<>(consumption, HttpStatus.CREATED);
		} catch (Exception e) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

	@PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
	@GetMapping("/by-environment/{env}")
	/**
	 * Devuelve una página de los consumos de un ambiente.
	 *
	 * <p>Lo consumen las pantallas de «Consumos por ambiente» del gestor y del administrador, que son componentes
	 * distintos invocando <strong>este mismo endpoint</strong> con los mismos permisos.
	 *
	 * <p>El ambiente se compara por igualdad exacta y no hay catálogo que lo valide: una errata devuelve una página vacía
	 * sin indicar la causa.
	 *
	 * @param env  nombre exacto del ambiente
	 * @param from inicio del rango, opcional
	 * @param to   fin del rango, opcional
	 * @param page índice de página, base cero
	 * @param size tamaño de página
	 * @return {@code 200} con la página; {@code 400} si el rango excede tres meses
	 */
	public ResponseEntity<?> getByEnvironment(
			@PathVariable String env,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {
		if (from != null && to != null && ChronoUnit.DAYS.between(from, to) > 92) {
			return ResponseEntity.badRequest().body(Map.of("message", "El rango no puede superar 3 meses"));
		}
		int safePage = Math.max(page, 0);
		int safeSize = Math.min(Math.max(size, 1), 100);
		Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "consumptionOpening"));
		return ResponseEntity.ok(consumptionServ.getByEnviromentPaged(env, from, to, pageable));
	}

	@GetMapping("/by-partner/{partnerId}")
	/**
	 * Devuelve todos los consumos de un socio, sin paginar, previa comprobación de propiedad.
	 *
	 * <p>A diferencia de los endpoints {@code /me}, acepta un identificador del cliente, y por eso necesita una guarda
	 * explícita: {@link #canAccessPartner(Long)} permite el acceso a los roles privilegiados y, al resto, solo a sus propios
	 * consumos.
	 *
	 * <p>El frontend no lo usa —prefiere las variantes por identificación, que además paginan—, pero permanece expuesto.
	 *
	 * @param partnerId clave primaria del socio
	 * @return {@code 200} con los consumos; {@code 204} si no tiene ninguno; {@code 403} si el solicitante no puede acceder
	 *         a ese socio; {@code 404} si no existe
	 */
	public ResponseEntity<List<PartnerConsumption>> getByPartner(@PathVariable Long partnerId) {
		if (!canAccessPartner(partnerId)) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
		List<PartnerConsumption> list = consumptionServ.getByPartnerId(partnerId);
		if (list == null)
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		if (list.isEmpty())
			return new ResponseEntity<>(list, HttpStatus.NO_CONTENT);
		return new ResponseEntity<>(list, HttpStatus.OK);
	}

	/**
	 * Determina si el usuario autenticado puede consultar los consumos del socio indicado.
	 *
	 * <p>Es la <strong>única guarda explícita contra IDOR del sistema</strong>, introducida para corregir precisamente ese
	 * defecto. La regla es simple: los roles {@code MANAGER} y {@code ADMIN} acceden a cualquier socio; el resto solo a sí
	 * mismos, comparando el identificador recibido con el del principal resuelto desde el contexto de seguridad.
	 *
	 * <p>Nótese que comprueba los roles <strong>comparando cadenas crudas</strong> ({@code "ROLE_MANAGER"}), mientras el
	 * resto del sistema usa las expresiones {@code hasAnyRole(...)} de {@code @PreAuthorize}, que añaden el prefijo por su
	 * cuenta. Son dos idiomas distintos para la misma comprobación, y deben mantenerse sincronizados a mano.
	 *
	 * @param partnerId clave primaria del socio que se pretende consultar
	 * @return {@code true} si el acceso está permitido
	 */
	private boolean canAccessPartner(Long partnerId) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null || !auth.isAuthenticated()) {
			return false;
		}
		boolean privileged = auth.getAuthorities().stream()
				.anyMatch(a -> "ROLE_MANAGER".equals(a.getAuthority()) || "ROLE_ADMIN".equals(a.getAuthority()));
		if (privileged) {
			return true;
		}
		if (!(auth.getPrincipal() instanceof UserDetails)) {
			return false;
		}
		String identification = ((UserDetails) auth.getPrincipal()).getUsername();
		PersonPartner me = personPartnerService.getByIdentification(identification);
		return me != null && partnerId.equals(me.getPersonId());
	}
}
