package co.edu.unbosque.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Fija el comportamiento del codificador de contraseñas, incluida su rampa de compatibilidad.
 *
 * <p><strong>Es la prueba más importante de documentar de todo el conjunto</strong>, porque consagra un comportamiento que a
 * primera vista parece un defecto: afirma explícitamente que una contraseña heredada almacenada <strong>en texto plano se
 * valida correctamente</strong>, y que debe marcarse para recodificación.
 *
 * <p>Quien «corrija» el codificador retirando ese respaldo hará fallar esta prueba, y con razón: dejaría fuera del sistema a
 * todos los socios cuya contraseña nunca se ha recodificado. La forma correcta de cerrar la rampa es migrar esas filas
 * primero.
 */
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
