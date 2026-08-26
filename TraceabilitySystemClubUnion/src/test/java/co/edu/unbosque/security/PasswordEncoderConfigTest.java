package co.edu.unbosque.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordEncoderConfigTest {

	private PasswordEncoder encoder;

	@BeforeEach
	void setUp() {
		encoder = new SecurityConfig().passwordEncoder();
	}

	@Test
	void encode_producesBcryptHashWithPrefix() {
		String encoded = encoder.encode("secret123");
		assertTrue(encoded.startsWith("{bcrypt}"));
		assertNotEquals("secret123", encoded);
	}

	@Test
	void matches_acceptsLegacyPlaintextPassword() {
		assertTrue(encoder.matches("hassed_pass_1", "hassed_pass_1"));
	}

	@Test
	void matches_acceptsBcryptEncodedPassword() {
		String encoded = encoder.encode("secret123");
		assertTrue(encoder.matches("secret123", encoded));
	}

	@Test
	void matches_rejectsWrongPassword() {
		String encoded = encoder.encode("secret123");
		assertFalse(encoder.matches("otraClave", encoded));
		assertFalse(encoder.matches("otraClave", "hassed_pass_1"));
	}

	@Test
	void upgradeEncoding_isTrueForLegacyPlaintextAndFalseForBcrypt() {
		assertTrue(encoder.upgradeEncoding("hassed_pass_1"));
		assertFalse(encoder.upgradeEncoding(encoder.encode("secret123")));
	}
}
