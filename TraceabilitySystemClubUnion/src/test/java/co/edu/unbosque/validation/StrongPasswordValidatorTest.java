package co.edu.unbosque.validation;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifica cada regla de la política de contraseñas por separado.
 *
 * <p>Comprueba longitud, minúscula, mayúscula y dígito de forma independiente, de modo que un cambio en la expresión regular
 * que debilite una sola condición se detecta con precisión.
 *
 * <p>Fija además que <strong>un valor nulo es válido</strong>, que es la convención de validación: la obligatoriedad es
 * responsabilidad de otra restricción, y por eso ambos DTO combinan las dos.
 */
class StrongPasswordValidatorTest {

	private final StrongPasswordValidator validator = new StrongPasswordValidator();

	@Test
	void validPassword_meetsAllRules() {
		assertTrue(validator.isValid("Abcdefghij12", null));
	}

	@Test
	void tooShort_isInvalid() {
		assertFalse(validator.isValid("Abc12", null));
	}

	@Test
	void missingUppercase_isInvalid() {
		assertFalse(validator.isValid("abcdefghij12", null));
	}

	@Test
	void missingLowercase_isInvalid() {
		assertFalse(validator.isValid("ABCDEFGHIJ12", null));
	}

	@Test
	void missingDigit_isInvalid() {
		assertFalse(validator.isValid("Abcdefghijkl", null));
	}

	@Test
	void nullValue_isValid_delegatesToNotBlank() {
		assertTrue(validator.isValid(null, null));
	}
}
