package co.unbosque.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

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
