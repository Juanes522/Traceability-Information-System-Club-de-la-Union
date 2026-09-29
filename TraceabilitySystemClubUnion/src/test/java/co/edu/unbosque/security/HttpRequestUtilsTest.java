package co.edu.unbosque.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Verifica la resolución de la dirección del cliente.
 *
 * <p>Fija tres comportamientos: que se prefiere el primer salto de la cabecera de reenvío, que en su ausencia se usa la
 * dirección del socket, y que fuera de una petición devuelve nulo en lugar de fallar —lo que importa porque esta utilidad se
 * invoca desde código que puede ejecutarse sin petición asociada.
 *
 * <p>Nótese lo que <strong>no</strong> comprueba, porque el código tampoco lo hace: que la cabecera provenga de un origen de
 * confianza. La prueba equivalente del filtro de limitación de tasa sí lo exige, y esa diferencia es un hallazgo documentado.
 */
class HttpRequestUtilsTest {

    @AfterEach
    void clear() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void currentClientIp_usesFirstXForwardedForEntry() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("10.0.0.9");
        request.addHeader("X-Forwarded-For", "203.0.113.7, 70.41.3.18");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        assertEquals("203.0.113.7", HttpRequestUtils.currentClientIp());
    }

    @Test
    void currentClientIp_fallsBackToRemoteAddr() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("10.0.0.9");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        assertEquals("10.0.0.9", HttpRequestUtils.currentClientIp());
    }

    @Test
    void currentClientIp_returnsNullWhenNoRequestBound() {
        assertNull(HttpRequestUtils.currentClientIp());
    }
}
