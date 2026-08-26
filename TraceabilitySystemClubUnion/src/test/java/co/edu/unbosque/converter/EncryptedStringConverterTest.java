package co.edu.unbosque.converter;

import co.edu.unbosque.security.AesGcmEncryptionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EncryptedStringConverterTest {

	private EncryptedStringConverter converter;

	@BeforeEach
	void setUp() {
		byte[] keyBytes = new byte[32];
		Arrays.fill(keyBytes, (byte) 'A');
		AesGcmEncryptionService service = new AesGcmEncryptionService(
				Base64.getEncoder().encodeToString(keyBytes));
		converter = new EncryptedStringConverter(service);
	}

	@Test
	void roundTrip_encryptsForDbAndDecryptsForEntity() {
		String stored = converter.convertToDatabaseColumn("0991234567");
		assertTrue(stored.startsWith("ENC:v1:"));
		assertEquals("0991234567", converter.convertToEntityAttribute(stored));
	}

	@Test
	void read_returnsLegacyPlaintextUnchanged() {
		assertEquals("042345678", converter.convertToEntityAttribute("042345678"));
	}

	@Test
	void nullAndEmpty_passThrough() {
		assertNull(converter.convertToDatabaseColumn(null));
		assertNull(converter.convertToEntityAttribute(null));
		assertEquals("", converter.convertToDatabaseColumn(""));
		assertEquals("", converter.convertToEntityAttribute(""));
	}
}
