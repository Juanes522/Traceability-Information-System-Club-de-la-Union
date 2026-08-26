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

@RestController
@RequestMapping("/push")
public class PushSubscriptionController {

	private final PushSubscriptionRepository subRepo;
	private final PersonPartnerService partnerServ;
	private final PushNotificationService pushService;

	public PushSubscriptionController(PushSubscriptionRepository subRepo, PersonPartnerService partnerServ,
			PushNotificationService pushService) {
		this.subRepo = subRepo;
		this.partnerServ = partnerServ;
		this.pushService = pushService;
	}

	@GetMapping("/vapid-public-key")
	public ResponseEntity<String> getVapidPublicKey() {
		return ResponseEntity.ok(pushService.getVapidPublicKey());
	}

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
