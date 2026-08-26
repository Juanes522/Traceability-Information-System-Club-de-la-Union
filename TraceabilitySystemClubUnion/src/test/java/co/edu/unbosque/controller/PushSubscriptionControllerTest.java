package co.edu.unbosque.controller;

import co.edu.unbosque.dto.PushSubscriptionRequest;
import co.edu.unbosque.model.PersonPartner;
import co.edu.unbosque.model.PushSubscription;
import co.edu.unbosque.repository.PushSubscriptionRepository;
import co.edu.unbosque.service.PersonPartnerService;
import co.edu.unbosque.service.PushNotificationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PushSubscriptionControllerTest {

	private PushSubscriptionRepository subRepo;
	private PersonPartnerService partnerServ;
	private PushSubscriptionController controller;

	@BeforeEach
	void setUp() {
		subRepo = mock(PushSubscriptionRepository.class);
		partnerServ = mock(PersonPartnerService.class);
		controller = new PushSubscriptionController(subRepo, partnerServ, mock(PushNotificationService.class));
	}

	@AfterEach
	void clearContext() {
		SecurityContextHolder.clearContext();
	}

	private void authenticateAs(String identification) {
		UserDetails principal = new User(identification, "hash", Collections.emptyList());
		SecurityContextHolder.getContext().setAuthentication(
				new UsernamePasswordAuthenticationToken(principal, null, Collections.emptyList()));
	}

	private PushSubscriptionRequest request(String endpoint) {
		PushSubscriptionRequest req = new PushSubscriptionRequest();
		req.setEndpoint(endpoint);
		req.setP256dhKey("p256");
		req.setAuthKey("auth");
		return req;
	}

	@Test
	void subscribe_returnsUnauthorized_whenNotAuthenticated() {
		ResponseEntity<Void> response = controller.subscribe(request("https://push/1"));
		assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
		verify(subRepo, never()).save(any());
	}

	@Test
	void subscribe_returnsNotFound_whenPartnerDoesNotExist() {
		authenticateAs("123");
		when(partnerServ.getByIdentification("123")).thenReturn(null);

		ResponseEntity<Void> response = controller.subscribe(request("https://push/1"));

		assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
		verify(subRepo, never()).save(any());
	}

	@Test
	void subscribe_createsSubscription_whenEndpointIsNew() {
		authenticateAs("123");
		when(partnerServ.getByIdentification("123")).thenReturn(new PersonPartner());
		when(subRepo.findByEndpoint("https://push/1")).thenReturn(Optional.empty());

		ResponseEntity<Void> response = controller.subscribe(request("https://push/1"));

		assertEquals(HttpStatus.CREATED, response.getStatusCode());
		verify(subRepo).save(any(PushSubscription.class));
	}

	@Test
	void subscribe_isIdempotent_whenEndpointAlreadyExists() {
		authenticateAs("123");
		when(partnerServ.getByIdentification("123")).thenReturn(new PersonPartner());
		when(subRepo.findByEndpoint("https://push/1")).thenReturn(Optional.of(new PushSubscription()));

		ResponseEntity<Void> response = controller.subscribe(request("https://push/1"));

		assertEquals(HttpStatus.OK, response.getStatusCode());
		verify(subRepo, never()).save(any());
	}

	@Test
	void unsubscribe_deletesByEndpointAndReturnsNoContent() {
		ResponseEntity<Void> response = controller.unsubscribe(request("https://push/9"));

		assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
		verify(subRepo).deleteByEndpoint("https://push/9");
	}
}
