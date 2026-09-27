package co.edu.unbosque.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import co.edu.unbosque.model.PushSubscription;
import co.edu.unbosque.repository.PushSubscriptionRepository;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import nl.martijndwars.webpush.Subscription;
import nl.martijndwars.webpush.Subscription.Keys;

/**
 * Entrega de notificaciones Web Push a los navegadores de los socios.
 *
 * <p>Usa la biblioteca {@code web-push} con firma VAPID. Su único disparador es el registro de un consumo:
 * {@link PartnerConsumptionService#register} lo invoca para avisar al socio en el momento del cargo.
 *
 * <p><strong>Requiere el proveedor criptográfico BouncyCastle</strong>, que se registra en el {@code main()} de
 * la aplicación. Conviene saber que en un despliegue como WAR sobre un Tomcat externo ese {@code main()} no se
 * ejecuta, de modo que el proveedor no queda registrado y la firma dependería de lo que ofrezca el contenedor.
 *
 * <p>Los fallos de entrega se capturan por suscripción y se escriben en {@code System.err}: un cargo se registra
 * aunque ninguna notificación llegue. Es coherente con la prioridad del sistema —el dato contable por delante del
 * aviso—, pero implica que los problemas de entrega son invisibles.
 *
 * @see co.edu.unbosque.model.PushSubscription
 * @see co.edu.unbosque.controller.PushSubscriptionController
 */
@Service
public class PushNotificationService {

	@Value("${vapid.public.key}")
	private String vapidPublicKey;

	@Value("${vapid.private.key}")
	private String vapidPrivateKey;

	@Value("${vapid.subject}")
	private String vapidSubject;

	private final PushSubscriptionRepository subscriptionRepo;

	public PushNotificationService(PushSubscriptionRepository subscriptionRepo) {
		this.subscriptionRepo = subscriptionRepo;
	}

	/**
	 * Devuelve la clave pública VAPID.
	 *
	 * <p>El navegador la necesita <strong>antes</strong> de poder suscribirse, y es por eso que
	 * {@code GET /push/vapid-public-key} es uno de los tres endpoints públicos del sistema. La mitad pública de un
	 * par VAPID es pública por diseño; exponerla no compromete nada.
	 *
	 * @return la clave pública en el formato que espera la API Push del navegador
	 */
	public String getVapidPublicKey() {
		return vapidPublicKey;
	}

	/**
	 * Envía una notificación a todas las suscripciones del socio.
	 *
	 * <p>Un socio puede tener varias suscripciones —una por navegador o dispositivo—, y el aviso se difunde a
	 * todas. Los fallos se aíslan por suscripción: un endpoint caído no impide la entrega a los demás.
	 *
	 * <p>Consideraciones técnicas que conviene conocer antes de modificar este método:
	 *
	 * <ul>
	 *   <li><strong>Es sincrónico y se invoca dentro de la transacción</strong> del registro del consumo, de modo
	 *       que mantiene abiertos los bloqueos y la conexión durante N peticiones HTTPS a servicios externos.</li>
	 *   <li>Construye un {@code PushService} nuevo <strong>por cada suscripción y cada mensaje</strong>, con la
	 *       preparación de claves de curva elíptica que eso implica.</li>
	 *   <li><strong>No depura las suscripciones muertas.</strong> Las respuestas 404 y 410 de los servicios push
	 *       —que significan «este endpoint ya no existe»— se tratan como cualquier otro error y se descartan, así
	 *       que los endpoints caducados se reintentan indefinidamente.</li>
	 *   <li>El cuerpo JSON se compone por concatenación de cadenas, con un escape que cubre solo la barra invertida
	 *       y la comilla doble. Es seguro hoy porque título y cuerpo los genera el servidor con un formato fijo;
	 *       un salto de línea o un carácter de control en ellos produciría <strong>JSON inválido</strong>.</li>
	 * </ul>
	 *
	 * @param partnerIdentification identificación del socio destinatario, en claro
	 * @param title                 título de la notificación
	 * @param body                  cuerpo de la notificación
	 */
	public void sendToPartner(String partnerIdentification, String title, String body) {
		List<PushSubscription> subs = subscriptionRepo.findByPartnerIdentification(partnerIdentification);
		if (subs == null || subs.isEmpty())
			return;

		String payload = "{\"notification\":{\"title\":\"" + escapeJson(title)
			+ "\",\"body\":\"" + escapeJson(body)
			+ "\",\"icon\":\"/assets/ClubIcon.png\"}}";

		for (PushSubscription sub : subs) {
			try {
				PushService pushService = new PushService(vapidPublicKey, vapidPrivateKey, vapidSubject);
				Subscription subscription = new Subscription(sub.getEndpoint(),
						new Keys(sub.getP256dhKey(), sub.getAuthKey()));
				Notification notification = new Notification(subscription, payload);
				pushService.send(notification);
			} catch (Exception e) {
				System.err.println("Push failed for " + sub.getEndpoint() + ": " + e.getMessage());
			}
		}
	}

	private String escapeJson(String value) {
		if (value == null)
			return "";
		return value.replace("\\", "\\\\").replace("\"", "\\\"");
	}
}
