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

/**
 * Verifica la entrega de notificaciones Web Push y el escape del contenido.
 *
 * <p>Comprueba que un socio sin suscripciones no provoca error, y que el escape protege las comillas y las barras invertidas
 * del texto.
 *
 * <p>Nótese el alcance de esa última comprobación: cubre los caracteres que romperían la estructura del mensaje, pero
 * <strong>no</strong> los saltos de línea ni los caracteres de control, que también producirían un mensaje inválido. Hoy no se
 * manifiesta porque el título y el cuerpo los genera el servidor con un formato fijo.
 */
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
