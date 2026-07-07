package co.unbosque.controller;

import co.unbosque.security.JwtUtil;
import co.unbosque.service.PersonPartnerService;
import co.unbosque.service.RateLimitService;
import co.unbosque.service.TokenBlacklistService;
import co.unbosque.repository.PasswordResetTokenRepository;
import co.unbosque.service.EmailService;
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
    private AuthController controller;

    @BeforeEach
    void setUp() {
        jwtUtil = mock(JwtUtil.class);
        tokenBlacklist = mock(TokenBlacklistService.class);
        controller = new AuthController(
                mock(AuthenticationManager.class),
                mock(UserDetailsService.class),
                jwtUtil,
                mock(PersonPartnerService.class),
                mock(org.springframework.security.crypto.password.PasswordEncoder.class),
                mock(PasswordResetTokenRepository.class),
                mock(EmailService.class),
                mock(RateLimitService.class),
                tokenBlacklist);
    }

    @Test
    void logout_revokesJtiFromAuthorizationHeader() {
        Date expiry = new Date(System.currentTimeMillis() + 3600000);
        when(jwtUtil.extractJti("token-xyz")).thenReturn("jti-xyz");
        when(jwtUtil.extractExpiration("token-xyz")).thenReturn(expiry);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer token-xyz");

        ResponseEntity<?> response = controller.logout(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(tokenBlacklist).revoke("jti-xyz",
                expiry.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime());
    }

    @Test
    void logout_withoutBearerToken_returnsOkAndDoesNotRevoke() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        ResponseEntity<?> response = controller.logout(request);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
