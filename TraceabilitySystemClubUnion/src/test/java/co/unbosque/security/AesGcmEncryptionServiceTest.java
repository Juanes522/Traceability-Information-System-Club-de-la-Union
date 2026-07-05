package co.unbosque.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AesGcmEncryptionServiceTest {

	private static String testKey() {
		byte[] keyBytes = new byte[32];
		Arrays.fill(keyBytes, (byte) 'A');
		return Base64.getEncoder().encodeToString(keyBytes);
	}

	private AesGcmEncryptionService service;

	@BeforeEach
	void setUp() {
		service = new AesGcmEncryptionService(testKey());
	}

	@Test
	void encrypt_producesPrefixedValueAndDecryptRoundTrips() {
		String encrypted = service.encrypt("juan@example.com");
		assertTrue(encrypted.startsWith("ENC:v1:"));
		assertEquals("juan@example.com", service.decrypt(encrypted));
	}

	@Test
	void encrypt_usesRandomIvSoSamePlaintextGivesDifferentCiphertext() {
		assertNotEquals(service.encrypt("mismoValor"), service.encrypt("mismoValor"));
	}

	@Test
	void decrypt_returnsLegacyPlaintextUnchanged() {
		// Datos previos a la migración: sin prefijo ENC:v1: → se devuelven tal cual
		assertEquals("0999999999", service.decrypt("0999999999"));
	}

	@Test
	void nullValues_passThrough() {
		assertNull(service.encrypt(null));
		assertNull(service.decrypt(null));
	}

	@Test
	void decrypt_failsOnTamperedCiphertext() {
		String encrypted = service.encrypt("dato sensible");
		String tampered = encrypted.substring(0, encrypted.length() - 5) + "AAAA=";
		assertThrows(IllegalStateException.class, () -> service.decrypt(tampered));
	}

	@Test
	void decrypt_failsOnTruncatedCiphertext() {
		assertThrows(IllegalStateException.class, () -> service.decrypt("ENC:v1:AAAA"));
	}

	@Test
	void constructor_rejectsKeyOfWrongLength() {
		String shortKey = Base64.getEncoder().encodeToString(new byte[16]);
		assertThrows(IllegalStateException.class, () -> new AesGcmEncryptionService(shortKey));
	}

	@Test
	void constructor_rejectsNonBase64Key() {
		assertThrows(IllegalStateException.class, () -> new AesGcmEncryptionService("no-es-base64-válido!!!"));
	}
}
