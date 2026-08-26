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

	public String getVapidPublicKey() {
		return vapidPublicKey;
	}

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
