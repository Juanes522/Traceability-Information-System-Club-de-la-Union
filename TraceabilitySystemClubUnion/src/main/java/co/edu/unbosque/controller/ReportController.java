package co.edu.unbosque.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.model.PersonPartner;
import co.edu.unbosque.service.PersonPartnerService;
import co.edu.unbosque.service.ReportService;

/**
 * Endpoints de descarga de reportes en PDF.
 *
 * <p>Los cuatro devuelven {@code application/pdf} como flujo de bytes, con cabecera de descarga y un nombre de archivo que
 * incorpora la fecha de generación. No hay DTO de por medio: el documento se compone entero en
 * {@link co.edu.unbosque.service.ReportService} y se entrega tal cual.
 *
 * <p>A diferencia de los controladores de métricas, aquí {@code from} y {@code to} son <strong>obligatorios</strong>: un
 * reporte sin periodo declarado no tendría sentido como documento. Se mantiene el tope de 366 días.
 *
 * <p>El reporte de seguridad es el único reservado a {@code ADMIN}; los otros tres admiten también {@code MANAGER}.
 *
 * @see co.edu.unbosque.service.ReportService
 */
@RestController
@RequestMapping("/reports")
public class ReportController {

	private static final long MAX_RANGE_DAYS = 366;

	private final ReportService reports;
	private final PersonPartnerService partnerService;

	/**
	 * Crea una instancia con sus valores.
	 *
	 * @param reports el valor de reports
	 * @param partnerService el valor de partner service
	 */
	public ReportController(ReportService reports, PersonPartnerService partnerService) {
		this.reports = reports;
		this.partnerService = partnerService;
	}

	/**
	 * Descarga el reporte de consumos del periodo, opcionalmente restringido a un ambiente.
	 *
	 * <p>Es el reporte más completo: indicadores, gráficas por ambiente y por día, detalle de los consumos más recientes y
	 * tres secciones de analítica de producto.
	 *
	 * @param from inicio del periodo, obligatorio
	 * @param to fin del periodo, obligatorio
	 * @param environment nombre exacto del ambiente, o vacío para incluir todos
	 * @return {@code 200} con el PDF adjunto, o {@code 400} si el rango está invertido o excede 366 días
	 */
	@PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
	@GetMapping("/consumptions")
	public ResponseEntity<?> consumptions(
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
			@RequestParam(required = false) String environment) {
		if (invalid(from, to)) {
			return ResponseEntity.badRequest().build();
		}
		return pdf(reports.consumptionsPdf(from, to, environment), "consumos");
	}

	/**
	 * Descarga el reporte de ingresos por ambiente.
	 *
	 * <p>Parte de las métricas ya agregadas, de modo que sus cifras coinciden por construcción con las del tablero.
	 *
	 * @param from inicio del periodo, obligatorio
	 * @param to fin del periodo, obligatorio
	 * @return {@code 200} con el PDF adjunto, o {@code 400} si el rango es inválido
	 */
	@PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
	@GetMapping("/income-by-environment")
	public ResponseEntity<?> incomeByEnvironment(
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
		if (invalid(from, to)) {
			return ResponseEntity.badRequest().build();
		}
		return pdf(reports.incomeByEnvironmentPdf(from, to), "ingresos-por-ambiente");
	}

	/**
	 * Descarga el estado de cuenta de un socio.
	 *
	 * <p>Acepta <strong>dos formas de identificar al socio</strong> y exige exactamente una: su cédula o su número de
	 * acción. Cuando se usa el número de acción, el controlador resuelve el socio tomando el <strong>primero</strong> de la
	 * lista, porque ese número no es único: con una acción compartida por varias personas la resolución es arbitraria.
	 *
	 * <p>El documento resultante contiene datos personales descifrados —nombre y cédula—, de modo que hereda la
	 * sensibilidad de esa información.
	 *
	 * @param identification cédula del socio, o {@code null} si se identifica por acción
	 * @param shareNumber número de acción, o {@code null} si se identifica por cédula
	 * @param from inicio del periodo, obligatorio
	 * @param to fin del periodo, obligatorio
	 * @return {@code 200} con el PDF adjunto; {@code 400} si falta el identificador o el rango es inválido; {@code 404} si el
	 *         socio no existe
	 */
	@PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
	@GetMapping("/partner-statement")
	public ResponseEntity<?> partnerStatement(
			@RequestParam(required = false) String identification,
			@RequestParam(required = false) Long shareNumber,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
		if (invalid(from, to)) {
			return ResponseEntity.badRequest().build();
		}
		String resolved;
		if (identification != null && !identification.isBlank()) {
			resolved = identification;
		} else if (shareNumber != null) {
			List<PersonPartner> list = partnerService.getByShareNumber(shareNumber);
			if (list == null || list.isEmpty()) {
				return new ResponseEntity<>(HttpStatus.NOT_FOUND);
			}
			resolved = list.get(0).getIdentification();
		} else {
			return ResponseEntity.badRequest().build();
		}
		byte[] bytes = reports.partnerStatementPdf(resolved, from, to);
		if (bytes == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		return pdf(bytes, "estado-de-cuenta-" + resolved);
	}

	/**
	 * Descarga el reporte de seguridad del periodo.
	 *
	 * <p>Único reporte reservado a {@code ADMIN}, y único que se construye íntegramente sobre la bitácora de auditoría en
	 * Elasticsearch en lugar de la base relacional. Presenta los intentos fallidos agrupados por usuario y una tabla de
	 * eventos críticos.
	 *
	 * <p>Hereda los topes del servicio de consulta de la bitácora, de modo que en un periodo con muchos incidentes
	 * <strong>subestima sin indicarlo</strong>. Y, a diferencia del panel de seguridad, aquí un fallo de Elasticsearch
	 * propaga en lugar de degradar.
	 *
	 * @param from inicio del periodo, obligatorio
	 * @param to fin del periodo, obligatorio
	 * @return {@code 200} con el PDF adjunto, o {@code 400} si el rango es inválido
	 */
	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/security")
	public ResponseEntity<?> security(
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
		if (invalid(from, to)) {
			return ResponseEntity.badRequest().build();
		}
		return pdf(reports.securityPdf(from, to), "seguridad");
	}

	/**
	 * Comprueba si la ventana temporal solicitada es inaceptable.
	 *
	 * <p>Rechaza los rangos invertidos y los superiores a 366 días. A diferencia del método equivalente de los controladores
	 * de métricas, no aplica valores por defecto: en un reporte el periodo es obligatorio.
	 *
	 * @param from inicio del periodo
	 * @param to fin del periodo
	 * @return {@code true} si el rango debe rechazarse
	 */
	private boolean invalid(LocalDateTime from, LocalDateTime to) {
		return from.isAfter(to) || ChronoUnit.DAYS.between(from, to) > MAX_RANGE_DAYS;
	}

	private ResponseEntity<byte[]> pdf(byte[] bytes, String name) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_PDF);
		headers.setContentDisposition(ContentDisposition.attachment()
				.filename(name + "-" + LocalDate.now() + ".pdf").build());
		return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
	}
}
