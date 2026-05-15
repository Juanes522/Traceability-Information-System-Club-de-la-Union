package co.unbosque.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import co.unbosque.dto.PushSubscriptionRequest;
import co.unbosque.model.PersonPartner;
import co.unbosque.model.PushSubscription;
import co.unbosque.repository.PersonPartnerRepository;
import co.unbosque.repository.PushSubscriptionRepository;
import co.unbosque.service.PushNotificationService;

@RestController
@RequestMapping("/push")
public class PushSubscriptionController {

	private final PushSubscriptionRepository subRepo;
	private final PersonPartnerRepository partnerRepo;
	private final PushNotificationService pushService;

	public PushSubscriptionController(PushSubscriptionRepository subRepo, PersonPartnerRepository partnerRepo,
			PushNotificationService pushService) {
		this.subRepo = subRepo;
		this.partnerRepo = partnerRepo;
		this.pushService = pushService;
	}

	@GetMapping("/vapid-public-key")
	public ResponseEntity<String> getVapidPublicKey() {
		return ResponseEntity.ok(pushService.getVapidPublicKey());
	}

	@PostMapping("/subscribe")
	public ResponseEntity<Void> subscribe(@RequestBody PushSubscriptionRequest req) {
		String identification = currentIdentification();
		if (identification == null)
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

		PersonPartner partner = partnerRepo.findByIdentification(identification).orElse(null);
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
