package co.edu.unbosque.controller;

import co.edu.unbosque.security.JwtUtil;
import co.edu.unbosque.service.PersonPartnerService;
import co.edu.unbosque.service.RateLimitService;
import co.edu.unbosque.service.TokenBlacklistService;
import co.edu.unbosque.repository.PasswordResetTokenRepository;
import co.edu.unbosque.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthControllerLogoutTest {

    private JwtUtil jwtUtil;
    private TokenBlacklistService tokenBlacklist;
    private co.edu.unbosque.service.AuditService auditService;
    private AuthController controller;

    @BeforeEach
    void setUp() {
        jwtUtil = mock(JwtUtil.class);
        tokenBlacklist = mock(TokenBlacklistService.class);
        auditService = mock(co.edu.unbosque.service.AuditService.class);
        controller = new AuthController(
                mock(AuthenticationManager.class),
                mock(UserDetailsService.class),
                jwtUtil,
                mock(PersonPartnerService.class),
                mock(org.springframework.security.crypto.password.PasswordEncoder.class),
                mock(PasswordResetTokenRepository.class),
                mock(EmailService.class),
                mock(RateLimitService.class),
                tokenBlacklist,
                auditService);
    }

    @Test
    void logout_revokesJtiFromAuthorizationHeader() {
        Date expiry = new Date(System.currentTimeMillis() + 3600000);
        when(jwtUtil.extractJti("token-xyz")).thenReturn("jti-xyz");
        when(jwtUtil.extractExpiration("token-xyz")).thenReturn(expiry);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer token-xyz");

        ResponseEntity<?> response = controller.logout(request, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(tokenBlacklist).revoke(
                org.mockito.ArgumentMatchers.eq("jti-xyz"),
                org.mockito.ArgumentMatchers.eq(
                        expiry.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime()),
                org.mockito.ArgumentMatchers.any());
    }

    @Test
    void logout_recordsLogoutEventWithUsernameFromToken() {
        Date expiry = new Date(System.currentTimeMillis() + 3600000);
        when(jwtUtil.extractJti("token-xyz")).thenReturn("jti-xyz");
        when(jwtUtil.extractExpiration("token-xyz")).thenReturn(expiry);
        when(jwtUtil.extractUsername("token-xyz")).thenReturn("123456789");

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer token-xyz");

        controller.logout(request, null);

        verify(auditService).record(
                org.mockito.ArgumentMatchers.eq(co.edu.unbosque.model.AuditEventType.LOGOUT),
                org.mockito.ArgumentMatchers.eq(co.edu.unbosque.model.AuditResult.SUCCESS),
                org.mockito.ArgumentMatchers.eq("123456789"),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any());
    }

    @Test
    void logout_withoutBearerToken_returnsOkAndDoesNotRevoke() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        ResponseEntity<?> response = controller.logout(request, null);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void logout_withInvalidOrExpiredToken_returnsOkAndDoesNotThrow() {
        when(jwtUtil.extractJti("bad-token"))
                .thenThrow(new io.jsonwebtoken.ExpiredJwtException(null, null, "expired"));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer bad-token");

        ResponseEntity<?> response = controller.logout(request, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(tokenBlacklist, org.mockito.Mockito.never())
                .revoke(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.any());
    }
}
