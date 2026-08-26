package co.edu.unbosque.security;

import co.edu.unbosque.service.RateLimitService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;

class RateLimitFilterTest {

    private RateLimitService service;
    private RateLimitFilter filter;

    @BeforeEach
    void setUp() {
        service = new RateLimitService(2, 60, 5, 60, 1, 3600);
        filter = new RateLimitFilter(service, "");
    }

    private MockHttpServletResponse doPost(RateLimitFilter f, String uri, String remoteAddr, String forwardedFor) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", uri);
        request.setRemoteAddr(remoteAddr);
        if (forwardedFor != null) {
            request.addHeader("X-Forwarded-For", forwardedFor);
        }
        MockHttpServletResponse response = new MockHttpServletResponse();
        f.doFilter(request, response, new MockFilterChain());
        return response;
    }

    @Test
    void login_returns429WithRetryAfter_whenIpLimitExceeded() throws Exception {
        assertEquals(200, doPost(filter, "/auth/login", "10.0.0.1", null).getStatus());
        assertEquals(200, doPost(filter, "/auth/login", "10.0.0.1", null).getStatus());
        MockHttpServletResponse blocked = doPost(filter, "/auth/login", "10.0.0.1", null);
        assertEquals(429, blocked.getStatus());
        assertNotNull(blocked.getHeader("Retry-After"));
        assertTrue(blocked.getContentAsString().contains("Demasiados intentos"));
    }

    @Test
    void forgotPassword_usesItsOwnStricterBucket() throws Exception {
        assertEquals(200, doPost(filter, "/auth/forgot-password", "10.0.0.2", null).getStatus());
        assertEquals(429, doPost(filter, "/auth/forgot-password", "10.0.0.2", null).getStatus());
        assertEquals(200, doPost(filter, "/auth/login", "10.0.0.2", null).getStatus());
    }

    @Test
    void differentIps_haveIndependentLimits() throws Exception {
        doPost(filter, "/auth/login", "10.0.0.3", null);
        doPost(filter, "/auth/login", "10.0.0.3", null);
        assertEquals(429, doPost(filter, "/auth/login", "10.0.0.3", null).getStatus());
        assertEquals(200, doPost(filter, "/auth/login", "10.0.0.4", null).getStatus());
    }

    @Test
    void xForwardedFor_firstEntryIsUsedAsClientIp() throws Exception {
        RateLimitFilter proxyFilter = new RateLimitFilter(service, "10.0.0.5");
        doPost(proxyFilter, "/auth/login", "10.0.0.5", "203.0.113.7, 70.41.3.18");
        doPost(proxyFilter, "/auth/login", "10.0.0.5", "203.0.113.7, 70.41.3.18");
        assertEquals(429, doPost(proxyFilter, "/auth/login", "10.0.0.5", "203.0.113.7").getStatus());
        assertEquals(200, doPost(proxyFilter, "/auth/login", "10.0.0.5", "203.0.113.9").getStatus());
    }

    @Test
    void xForwardedFor_isIgnoredWhenSenderIsNotTrustedProxy() throws Exception {
        doPost(filter, "/auth/login", "10.0.0.6", "1.1.1.1");
        doPost(filter, "/auth/login", "10.0.0.6", "2.2.2.2");
        assertEquals(429, doPost(filter, "/auth/login", "10.0.0.6", "3.3.3.3").getStatus());
    }

    @Test
    void contextPath_isStrippedBeforeMatching() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/app/auth/login");
        request.setContextPath("/app");
        request.setRemoteAddr("10.0.0.7");
        assertFalse(filter.shouldNotFilter(request));
    }

    @Test
    void nonAuthPaths_areNotFiltered() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/partnerconsumption/list");
        assertTrue(filter.shouldNotFilter(request));
    }

    @Test
    void optionsPreflight_isNotFiltered() {
        MockHttpServletRequest request = new MockHttpServletRequest("OPTIONS", "/auth/login");
        assertTrue(filter.shouldNotFilter(request));
    }
}
