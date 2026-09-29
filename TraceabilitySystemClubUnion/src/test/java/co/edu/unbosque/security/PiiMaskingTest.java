package co.edu.unbosque.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Verifica el enmascaramiento de correos y sus casos límite.
 *
 * <p>Confirma que se conservan la primera letra y el dominio —lo justo para diagnosticar sin identificar— y que los valores
 * degenerados no producen excepción.
 */
class PiiMaskingTest {

    @Test
    void maskEmail_keepsFirstCharAndDomain() {
        assertEquals("a***@example.com", PiiMasking.maskEmail("ana@example.com"));
        assertEquals("j***@dominio.co", PiiMasking.maskEmail("juan@dominio.co"));
    }

    @Test
    void maskEmail_handlesNullAndMissingAtSign() {
        assertNull(PiiMasking.maskEmail(null));
        assertEquals("***", PiiMasking.maskEmail("sinarroba"));
    }
}
