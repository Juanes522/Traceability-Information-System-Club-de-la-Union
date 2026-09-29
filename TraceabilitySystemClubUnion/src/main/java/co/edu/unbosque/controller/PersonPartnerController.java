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
import org.springframework.web.bind.annotation.*;

import co.edu.unbosque.model.PersonPartner;
import co.edu.unbosque.service.PartnerConsumptionService;
import co.edu.unbosque.service.PersonPartnerService;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Endpoints de consulta de socios, de datos propios y de sincronización con el padrón externo.
 *
 * <p>Agrupa tres familias con niveles de privilegio muy distintos:
 *
 * <ol>
 *   <li><strong>Búsqueda y listado</strong> — reservado a {@code MANAGER} y {@code ADMIN}.</li>
 *   <li><strong>Datos propios</strong> ({@code /me}, {@code /notifications/me}, {@code /my-logins}) — cualquier usuario
 *       autenticado, sobre sí mismo. La identidad se resuelve del contexto de seguridad y <strong>el cliente nunca envía
 *       un identificador</strong>, lo que hace el acceso estructuralmente seguro sin necesidad de guardas explícitas.</li>
 *   <li><strong>Sincronización</strong> — reservado a {@code ADMIN}.</li>
 * </ol>
 *
 * <h2>Advertencia: estos endpoints devuelven la entidad, no un DTO</h2>
 *
 * <p>Los siete métodos que devuelven {@link PersonPartner} serializan <strong>la entidad JPA directamente</strong>. Las
 * consecuencias están detalladas en esa clase, y son dos: el hash de la contraseña viaja al cliente, y los conversores
 * descifran al leer, de modo que la identificación, los teléfonos y los correos se entregan <strong>en claro</strong>.
 *
 * <h2>Política de rango temporal</h2>
 *
 * <p>Este controlador acota las ventanas a <strong>92 días</strong>, con el tope escrito en línea en cada método y sin
 * ventana por defecto. Es una política distinta de la de los controladores de métricas y reportes, que usan 366 días con
 * una ventana por defecto de 30. La diferencia no está documentada en el código.
 *
 * @see co.edu.unbosque.service.PersonPartnerService
 * @see co.edu.unbosque.service.PartnerSyncService
 */
@RestController
@RequestMapping("/personpartner")
public class PersonPartnerController {

	@Autowired
	private PersonPartnerService partnerServ;

	@Autowired
	private PartnerConsumptionService consumptionServ;

	@Autowired
	private co.edu.unbosque.service.AuditQueryService auditQuery;

	@Autowired
	private co.edu.unbosque.service.PartnerSyncService partnerSyncService;

	/**
	 * Constructor sin argumentos requerido para la deserialización del cuerpo de la petición.
	 */
	public PersonPartnerController() {
	}

	/**
	 * Busca un socio por su identificación.
	 *
	 * <p>Nótese que la respuesta 404 se construye <strong>con el cuerpo nulo del propio resultado</strong>, de modo que el
	 * cliente recibe un 404 sin contenido en lugar de un mensaje de error.
	 *
	 * @param identification cédula del socio
	 * @return {@code 200} con el socio, o {@code 404} si no existe
	 */
	@PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
	@GetMapping(path = "/getbyidentification/{identification}")
	public ResponseEntity<PersonPartner> getByIdentification(@PathVariable String identification) {
		PersonPartner partner = partnerServ.getByIdentification(identification);
		if (partner == null) {
			return new ResponseEntity<>(partner, HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<>(partner, HttpStatus.OK);
	}

	/**
	 * Busca socios por su primer nombre, con coincidencia exacta.
	 *
	 * @param firstname primer nombre exacto; no admite búsqueda parcial
	 * @return {@code 200} con los socios, o {@code 204} si no hay coincidencias
	 */
	@PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
	@GetMapping(path = "/getbyfirstname/{firstname}")
	public ResponseEntity<List<PersonPartner>> getByFirstName(@PathVariable String firstname) {
		List<PersonPartner> partners = partnerServ.getByFirstName(firstname);
		if (partners == null || partners.isEmpty()) {
			return new ResponseEntity<>(partners, HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(partners, HttpStatus.OK);
	}

	/**
	 * Busca socios por su segundo nombre, con coincidencia exacta.
	 *
	 * @param secondname segundo nombre exacto
	 * @return {@code 200} con los socios, o {@code 204} si no hay coincidencias
	 */
	@PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
	@GetMapping(path = "/getbysecondname/{secondname}")
	public ResponseEntity<List<PersonPartner>> getBySecondName(@PathVariable String secondname) {
		List<PersonPartner> partners = partnerServ.getBySecondName(secondname);
		if (partners == null || partners.isEmpty()) {
			return new ResponseEntity<>(partners, HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(partners, HttpStatus.OK);
	}

	/**
	 * Busca los socios asociados a un número de acción.
	 *
	 * <p>Devuelve una lista porque el número de acción <strong>no es único</strong>: varias personas pueden compartir una
	 * misma acción del club.
	 *
	 * @param sharenumber número de acción
	 * @return {@code 200} con los socios, o {@code 204} si no hay coincidencias
	 */
	@PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
	@GetMapping(path = "/getbysharenumber/{sharenumber}")
	public ResponseEntity<List<PersonPartner>> getByShareNumber(@PathVariable Long sharenumber) {
		List<PersonPartner> partners = partnerServ.getByShareNumber(sharenumber);
		if (partners == null || partners.isEmpty()) {
			return new ResponseEntity<>(partners, HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(partners, HttpStatus.OK);
	}

	/**
	 * Devuelve el padrón completo, sin paginar.
	 *
	 * <p>Lo consume la pantalla de búsqueda del gestor, que carga el padrón entero en memoria y pagina en el cliente. Con
	 * un padrón grande conviene preferir {@code /getallpaged}, que es lo que hace la pantalla de administración.
	 *
	 * @return {@code 200} con todos los socios, o {@code 204} si no hay ninguno
	 */
	@PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
	@GetMapping(path = "/getall")
	/**
	 * Devuelve el valor de all.
	 *
	 * @return el valor de all
	 */
	public ResponseEntity<List<PersonPartner>> getAll() {
		List<PersonPartner> partners = partnerServ.getAll();
		if (partners.isEmpty()) {
			return new ResponseEntity<>(partners, HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(partners, HttpStatus.OK);
	}

	/**
	 * Devuelve una página del padrón.
	 *
	 * <p>Los parámetros se <strong>acotan</strong> en lugar de rechazarse: la página no baja de cero y el tamaño se ajusta
	 * al intervalo de 1 a 100. Así una petición con valores absurdos devuelve datos válidos en vez de un error, y el tope
	 * de 100 impide que un cliente pida el padrón entero en una sola página.
	 *
	 * @param page índice de página, base cero
	 * @param size tamaño de página, acotado entre 1 y 100
	 * @return {@code 200} con la página solicitada
	 */
	@PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
	@GetMapping(path = "/getallpaged")
	public ResponseEntity<Page<PersonPartner>> getAllPaged(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {
		int safePage = Math.max(page, 0);
		int safeSize = Math.min(Math.max(size, 1), 100);
		return new ResponseEntity<>(partnerServ.getAllPaged(PageRequest.of(safePage, safeSize)), HttpStatus.OK);
	}

	/**
	 * Devuelve la ficha del usuario autenticado.
	 *
	 * <p>Sigue el patrón {@code /me}: la identidad se resuelve del contexto de seguridad, de modo que <strong>no acepta
	 * ningún identificador</strong> y es imposible consultar la ficha de otro. Por eso no necesita
	 * {@code @PreAuthorize} más allá de exigir autenticación.
	 *
	 * <p>Como el resto de los endpoints que devuelven la entidad, entrega los datos de contacto descifrados y el hash de
	 * la contraseña.
	 *
	 * @return {@code 200} con la ficha propia; {@code 401} si no hay usuario autenticado; {@code 404} si el socio no
	 *         existe
	 */
	@GetMapping(path = "/me")
	/**
	 * Devuelve el valor de me.
	 *
	 * @return el valor de me
	 */
	public ResponseEntity<PersonPartner> getMe() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() instanceof String) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}
		String identification = ((UserDetails) auth.getPrincipal()).getUsername();
		PersonPartner partner = partnerServ.getByIdentification(identification);

		if (partner == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}

		return new ResponseEntity<>(partner, HttpStatus.OK);
	}

	/**
	 * Devuelve una página de los consumos del propio socio, ordenados del más reciente al más antiguo.
	 *
	 * <p>Patrón {@code /me}: sin identificador en la petición.
	 *
	 * <p>El rango es opcional, pero si se envía <strong>no puede superar 92 días</strong>. Ese tope protege tanto la base
	 * como el tiempo de respuesta, dado que cada fila serializada provoca además una consulta adicional para resolver el
	 * número de acción.
	 *
	 * @param from inicio del rango, opcional
	 * @param to fin del rango, opcional
	 * @param page índice de página, base cero
	 * @param size tamaño de página
	 * @return {@code 200} con la página; {@code 400} si el rango excede tres meses; {@code 401} sin sesión; {@code 404} si
	 *         el socio no existe
	 */
	@GetMapping(path = "/getconsumptions/me")
	public ResponseEntity<?> getMyConsumptions(
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() instanceof String) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}
		if (from != null && to != null && ChronoUnit.DAYS.between(from, to) > 92) {
			return ResponseEntity.badRequest().body(Map.of("message", "El rango no puede superar 3 meses"));
		}
		String identification = ((UserDetails) auth.getPrincipal()).getUsername();
		PersonPartner partner = partnerServ.getByIdentification(identification);
		if (partner == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		int safePage = Math.max(page, 0);
		int safeSize = Math.min(Math.max(size, 1), 100);
		Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "consumptionOpening"));
		return ResponseEntity.ok(consumptionServ.getByPartnerPaged(partner.getPersonId(), from, to, pageable));
	}

	/**
	 * Devuelve una página de los consumos de un socio indicado por su identificación.
	 *
	 * <p>Es la variante privilegiada de {@link #getMyConsumptions}: aquí sí se acepta un identificador, y lo que impide el
	 * acceso indebido es el {@code @PreAuthorize} de rol, no una guarda de propiedad.
	 *
	 * @param identification cédula del socio a consultar
	 * @param from inicio del rango, opcional
	 * @param to fin del rango, opcional
	 * @param page índice de página, base cero
	 * @param size tamaño de página
	 * @return {@code 200} con la página; {@code 400} si el rango excede tres meses; {@code 404} si el socio no existe
	 */
	@PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
	@GetMapping(path = "/getconsumptionsidentification/{identification}")
	public ResponseEntity<?> getConsumptionsByIdentification(
			@PathVariable String identification,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {
		if (from != null && to != null && ChronoUnit.DAYS.between(from, to) > 92) {
			return ResponseEntity.badRequest().body(Map.of("message", "El rango no puede superar 3 meses"));
		}
		PersonPartner titular = partnerServ.getByIdentification(identification);
		if (titular == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		int safePage = Math.max(page, 0);
		int safeSize = Math.min(Math.max(size, 1), 100);
		Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "consumptionOpening"));
		return ResponseEntity.ok(consumptionServ.getByPartnerPaged(titular.getPersonId(), from, to, pageable));
	}

	/**
	 * Devuelve una página de los avisos del propio socio, del más reciente al más antiguo.
	 *
	 * <p>Patrón {@code /me}. Es la vista persistente de los cargos notificados: el correo y el mensaje push son entregas
	 * efímeras que pueden fallar, mientras que estos registros permanecen consultables.
	 *
	 * <p>No admite filtro temporal, a diferencia del listado de consumos.
	 *
	 * @param page índice de página, base cero
	 * @param size tamaño de página
	 * @return {@code 200} con la página de avisos; {@code 401} si no hay usuario autenticado
	 */
	@GetMapping("/notifications/me")
	public ResponseEntity<?> getMyNotifications(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() instanceof String) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}
		String identification = ((UserDetails) auth.getPrincipal()).getUsername();
		int safePage = Math.max(page, 0);
		int safeSize = Math.min(Math.max(size, 1), 100);
		return ResponseEntity.ok(consumptionServ.getNotificationsForPartnerPaged(identification, PageRequest.of(safePage, safeSize)));
	}

	/**
	 * Devuelve el historial de inicios de sesión exitosos del propio socio, con fecha e IP.
	 *
	 * <p>Es una función de seguridad para el usuario final: permite que cada socio detecte accesos que no reconoce.
	 *
	 * <p><strong>Es la única lectura de la bitácora de auditoría que no exige rol {@code ADMIN}.</strong> Lo que la hace
	 * segura es que el controlador fija el nombre de usuario desde el contexto de seguridad antes de consultar, de modo que
	 * el filtro no es manipulable por el cliente.
	 *
	 * <p>Nótese que la pantalla del frontend que lo consume se titula «Historial de accesos», pero muestra accesos al
	 * <strong>sistema</strong>, no entradas físicas al club: estas últimas viven en la tabla de accesos y no se exponen.
	 *
	 * @param page índice de página, base cero
	 * @param size tamaño de página
	 * @return {@code 200} con la página de accesos; {@code 401} si no hay usuario autenticado
	 */
	@GetMapping("/my-logins")
	public ResponseEntity<?> getMyLogins(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() instanceof String) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}
		String identification = ((UserDetails) auth.getPrincipal()).getUsername();
		int safePage = Math.max(page, 0);
		int safeSize = Math.min(Math.max(size, 1), 100);
		Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "timestamp"));
		Page<co.edu.unbosque.dto.LoginHistoryDTO> logins = auditQuery
				.search(identification, co.edu.unbosque.model.AuditEventType.LOGIN_SUCCESS, null, null, null, pageable)
				.map(e -> new co.edu.unbosque.dto.LoginHistoryDTO(e.getTimestamp(), e.getIpAddress()));
		return ResponseEntity.ok(logins);
	}

	/**
	 * Dispara la sincronización del padrón con el maestro externo de socios.
	 *
	 * <p><strong>Ninguna pantalla lo invoca:</strong> debe llamarse a la API directamente. Es una carencia de la interfaz de
	 * administración, no una decisión de diseño.
	 *
	 * <p>La operación no es transaccional en conjunto, no cuenta los fallos parciales y no deja evento de auditoría, pese a
	 * ser una mutación masiva de datos personales. El detalle está en
	 * {@link co.edu.unbosque.service.PartnerSyncService}.
	 *
	 * @return {@code 200} con el recuento de socios creados y actualizados; {@code 502} con un mensaje si el servicio
	 *         externo no responde o su respuesta no se puede procesar
	 */
	@PreAuthorize("hasRole('ADMIN')")
	@PostMapping("/sync")
	public ResponseEntity<?> sync() {
		try {
			return ResponseEntity.ok(partnerSyncService.sync());
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(Map.of("message", "No se pudo sincronizar socios: " + e.getMessage()));
		}
	}

}
