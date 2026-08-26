package co.edu.unbosque.converter;

import co.edu.unbosque.security.AesGcmEncryptionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EncryptedStringArrayConverterTest {

	private EncryptedStringArrayConverter converter;

	@BeforeEach
	void setUp() {
		byte[] keyBytes = new byte[32];
		Arrays.fill(keyBytes, (byte) 'A');
		AesGcmEncryptionService service = new AesGcmEncryptionService(
				Base64.getEncoder().encodeToString(keyBytes));
		converter = new EncryptedStringArrayConverter(service);
	}

	@Test
	void roundTrip_encryptsJoinedArrayAndRestoresIt() {
		String[] emails = { "juan@example.com", "maria@example.com" };
		String stored = converter.convertToDatabaseColumn(emails);
		assertTrue(stored.startsWith("ENC:v1:"));
		assertArrayEquals(emails, converter.convertToEntityAttribute(stored));
	}

	@Test
	void roundTrip_singleElementArray() {
		String[] emails = { "solo@example.com" };
		assertArrayEquals(emails,
				converter.convertToEntityAttribute(converter.convertToDatabaseColumn(emails)));
	}

	@Test
	void read_splitsLegacyPlaintextCsvUnchanged() {
		// Dato previo a la migración: CSV en claro escrito por StringArrayConverter
		assertArrayEquals(new String[] { "a@x.com", "b@x.com" },
				converter.convertToEntityAttribute("a@x.com,b@x.com"));
	}

	@Test
	void nullAndEmpty_matchLegacyConverterSemantics() {
		assertNull(converter.convertToDatabaseColumn(null));
		assertNull(converter.convertToDatabaseColumn(new String[0]));
		assertArrayEquals(new String[0], converter.convertToEntityAttribute(null));
		assertArrayEquals(new String[0], converter.convertToEntityAttribute(""));
	}

	@Test
	void storedValueIsNotPlaintext() {
		String stored = converter.convertToDatabaseColumn(new String[] { "juan@example.com" });
		assertEquals(-1, stored.indexOf("juan@example.com"));
	}
}
