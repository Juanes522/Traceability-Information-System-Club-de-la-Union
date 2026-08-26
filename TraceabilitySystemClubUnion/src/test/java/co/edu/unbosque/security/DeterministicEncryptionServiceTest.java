package co.edu.unbosque.security;

import org.junit.jupiter.api.Test;
import java.util.Base64;
import static org.junit.jupiter.api.Assertions.*;

class DeterministicEncryptionServiceTest {

	private final String KEY = Base64.getEncoder().encodeToString(new byte[32]);
	private final DeterministicEncryptionService svc = new DeterministicEncryptionService(KEY);

	@Test
	void encrypt_esDeterminista() {
		assertEquals(svc.encrypt("0102030405"), svc.encrypt("0102030405"));
	}

	@Test
	void roundTrip_descifraLoMismo() {
		String enc = svc.encrypt("0102030405");
		assertTrue(enc.startsWith("DET:v1:"));
		assertEquals("0102030405", svc.decrypt(enc));
	}

	@Test
	void distintasCedulas_distintoCiphertext() {
		assertNotEquals(svc.encrypt("111"), svc.encrypt("222"));
	}

	@Test
	void decrypt_toleraPlaintext() {
		assertEquals("9900009002", svc.decrypt("9900009002"));
	}

	@Test
	void nullYVacio_seDevuelvenIgual() {
		assertNull(svc.encrypt(null));
		assertEquals("", svc.encrypt(""));
	}
}
