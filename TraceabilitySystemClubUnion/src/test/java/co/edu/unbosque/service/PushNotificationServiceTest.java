package co.edu.unbosque.service;

import co.edu.unbosque.repository.PushSubscriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PushNotificationServiceTest {

	private PushSubscriptionRepository subscriptionRepo;
	private PushNotificationService service;

	@BeforeEach
	void setUp() {
		subscriptionRepo = mock(PushSubscriptionRepository.class);
		service = new PushNotificationService(subscriptionRepo);
		ReflectionTestUtils.setField(service, "vapidPublicKey", "public-key");
		ReflectionTestUtils.setField(service, "vapidPrivateKey", "private-key");
		ReflectionTestUtils.setField(service, "vapidSubject", "mailto:test@club.com");
	}

	@Test
	void getVapidPublicKey_returnsConfiguredValue() {
		assertEquals("public-key", service.getVapidPublicKey());
	}

	@Test
	void sendToPartner_doesNothing_whenPartnerHasNoSubscriptions() {
		when(subscriptionRepo.findByPartnerIdentification("123")).thenReturn(List.of());

		service.sendToPartner("123", "Titulo", "Cuerpo");

		verify(subscriptionRepo).findByPartnerIdentification("123");
	}

	@Test
	void escapeJson_escapesQuotesAndBackslashesToPreventPayloadInjection() {
		String escaped = ReflectionTestUtils.invokeMethod(service, "escapeJson", "a\"b\\c");
		assertEquals("a\\\"b\\\\c", escaped);
	}

	@Test
	void escapeJson_returnsEmptyStringForNull() {
		String escaped = ReflectionTestUtils.invokeMethod(service, "escapeJson", (Object) null);
		assertEquals("", escaped);
	}
}
