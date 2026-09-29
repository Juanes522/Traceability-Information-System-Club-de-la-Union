package co.edu.unbosque.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import co.edu.unbosque.dto.PushSubscriptionRequest;
import co.edu.unbosque.model.PersonPartner;
import co.edu.unbosque.model.PushSubscription;
import co.edu.unbosque.service.PersonPartnerService;
import co.edu.unbosque.repository.PushSubscriptionRepository;
import co.edu.unbosque.service.PushNotificationService;

import jakarta.validation.Valid;

/**
 * Endpoints de gestión de las suscripciones Web Push de los navegadores.
 *
 * <p><strong>Es el único controlador que rompe la arquitectura en capas:</strong> inyecta el repositorio y persiste
 * directamente, sin pasar por un servicio.
 *
 * <p>Tres operaciones con tres niveles de protección distintos, y el tercero es un defecto: la clave pública es
 * deliberadamente anónima, el alta comprueba el principal a mano, y la baja <strong>no comprueba nada</strong>.
 *
 * @see co.edu.unbosque.service.PushNotificationService
 * @see co.edu.unbosque.model.PushSubscription
 */
@RestController
@RequestMapping("/push")
public class PushSubscriptionController {

	private final PushSubscriptionRepository subRepo;
	private final PersonPartnerService partnerServ;
	private final PushNotificationService pushService;

	/**
	 * Crea el controlador con el repositorio de suscripciones y el servicio de socios.
	 *
	 * <p>Nótese que recibe un <strong>repositorio</strong> y no un servicio: es el unico controlador del sistema que persiste
	 * directamente, saltandose la capa de servicio.
	 *
	 * @param subRepo     repositorio de suscripciones push
	 * @param partnerServ acceso a los datos del socio, para asociar la suscripcion
	 * @param pushService servicio de entrega, del que se obtiene la clave publica
	 */
	public PushSubscriptionController(PushSubscriptionRepository subRepo, PersonPartnerService partnerServ,
			PushNotificationService pushService) {
		this.subRepo = subRepo;
		this.partnerServ = partnerServ;
		this.pushService = pushService;
	}

	/**
	 * Devuelve la clave pública VAPID que el navegador necesita para suscribirse.
	 *
	 * <p>Es uno de los tres endpoints públicos del sistema, y con razón: el navegador la requiere <strong>antes</strong> de
	 * poder crear la suscripción. La mitad pública de un par VAPID es pública por diseño y exponerla no compromete nada.
	 *
	 * @return {@code 200} con la clave en texto plano
	 */
	@GetMapping("/vapid-public-key")
	public ResponseEntity<String> getVapidPublicKey() {
		return ResponseEntity.ok(pushService.getVapidPublicKey());
	}

	/**
	 * Registra la suscripción Web Push del navegador del usuario autenticado.
	 *
	 * <p>Es <strong>idempotente</strong>: si el endpoint ya está almacenado responde 200 en lugar de crear un duplicado, lo
	 * que permite al cliente reintentar el alta sin comprobar antes si ya existe.
	 *
	 * <p>La suscripción se asocia al principal resuelto del contexto de seguridad, nunca a un identificador enviado por el
	 * cliente. La comprobación de autenticación es manual, igual que en las operaciones de cuenta.
	 *
	 * @param req endpoint y claves criptográficas que entrega la API Push del navegador
	 * @return {@code 201} si se creó; {@code 200} si ya estaba registrada; {@code 401} sin sesión; {@code 404} si el socio no
	 *         existe
	 */
	@PostMapping("/subscribe")
	public ResponseEntity<Void> subscribe(@Valid @RequestBody PushSubscriptionRequest req) {
		String identification = currentIdentification();
		if (identification == null)
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

		PersonPartner partner = partnerServ.getByIdentification(identification);
		if (partner == null)
			return ResponseEntity.notFound().build();

		if (subRepo.findByEndpoint(req.getEndpoint()).isPresent()) {
			return ResponseEntity.ok().build();
		}

		PushSubscription sub = new PushSubscription();
		sub.setEndpoint(req.getEndpoint());
		sub.setP256dhKey(req.getP256dhKey());
		sub.setAuthKey(req.getAuthKey());
		sub.setPartner(partner);
		subRepo.save(sub);

		return ResponseEntity.status(HttpStatus.CREATED).build();
	}

	/**
	 * Elimina una suscripción Web Push identificada por su endpoint.
	 *
	 * <p><strong>No comprueba la propiedad ni valida el cuerpo.</strong> A diferencia del alta, no verifica el principal, de
	 * modo que cualquier usuario autenticado que conozca el endpoint de otro puede eliminar su suscripción y silenciar sus
	 * avisos. Tampoco lleva anotación de validación, así que un endpoint nulo llega hasta la consulta.
	 *
	 * <p>Además, es probable que <strong>falle en ejecución</strong>: la operación de borrado del repositorio se declara sin
	 * transacción, y una consulta derivada de borrado la necesita.
	 *
	 * <p>Ninguna pantalla lo invoca —el cliente se da de baja solo en el navegador—, de modo que ni el defecto ni el riesgo
	 * se manifiestan en la práctica. La consecuencia observable es otra: las suscripciones nunca se depuran del servidor.
	 *
	 * @param req cuerpo del que se toma el endpoint a eliminar
	 * @return {@code 204} sin contenido
	 */
	@DeleteMapping("/unsubscribe")
	public ResponseEntity<Void> unsubscribe(@RequestBody PushSubscriptionRequest req) {
		subRepo.deleteByEndpoint(req.getEndpoint());
		return ResponseEntity.noContent().build();
	}

	private String currentIdentification() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() instanceof String)
			return null;
		return ((UserDetails) auth.getPrincipal()).getUsername();
	}
}
