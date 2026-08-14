package co.unbosque.controller;

import co.unbosque.dto.AuthRequest;
import co.unbosque.dto.AuthResponse;
import co.unbosque.dto.ChangePasswordRequest;
import co.unbosque.model.PersonPartner;
import co.unbosque.repository.PasswordResetTokenRepository;
import co.unbosque.security.JwtUtil;
import co.unbosque.service.EmailService;
import co.unbosque.service.PersonPartnerService;
import co.unbosque.service.RateLimitService;
import co.unbosque.service.TokenBlacklistService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthControllerTest {

    private AuthenticationManager authenticationManager;
    private UserDetailsService userDetailsService;
    private JwtUtil jwtUtil;
    private PersonPartnerService personPartnerService;
    private PasswordEncoder passwordEncoder;
    private RateLimitService rateLimitService;
    private co.unbosque.service.AuditService auditService;
    private AuthController controller;

    @BeforeEach
    void setUp() {
        authenticationManager = mock(AuthenticationManager.class);
        userDetailsService = mock(UserDetailsService.class);
        jwtUtil = mock(JwtUtil.class);
        personPartnerService = mock(PersonPartnerService.class);
        passwordEncoder = mock(PasswordEncoder.class);
        rateLimitService = mock(RateLimitService.class);
        auditService = mock(co.unbosque.service.AuditService.class);
        controller = new AuthController(authenticationManager, userDetailsService, jwtUtil,
                personPartnerService, passwordEncoder, mock(PasswordResetTokenRepository.class),
                mock(EmailService.class), rateLimitService, mock(TokenBlacklistService.class), auditService);
    }

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    private UserDetails userDetails(String username) {
        return new User(username, "hash", Collections.emptyList());
    }

    @Test
    void login_success_returnsTokenRoleAndForcePasswordChangeFlag() {
        AuthRequest req = new AuthRequest();
        req.setIdentification("123");
        req.setPassword("secreta1");

        when(rateLimitService.isUserBlocked("123")).thenReturn(false);
        when(userDetailsService.loadUserByUsername("123")).thenReturn(userDetails("123"));
        when(jwtUtil.generateToken(org.mockito.ArgumentMatchers.any())).thenReturn("jwt-abc");

        PersonPartner partner = new PersonPartner();
        partner.setIdentification("123");
        partner.setRole("ADMIN");
        partner.setForcePasswordChange(false);
        when(personPartnerService.getByIdentification("123")).thenReturn(partner);

        ResponseEntity<?> response = controller.createAuthenticationToken(req);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        AuthResponse body = (AuthResponse) response.getBody();
        assertEquals("jwt-abc", body.getToken());
        assertEquals("ROLE_ADMIN", body.getRole());
        assertEquals(false, body.getNeedsPasswordChange());
        verify(rateLimitService).resetUserFailures("123");
    }

    @Test
    void login_returnsNotFound_whenPartnerMissingAfterAuthentication() {
        AuthRequest req = new AuthRequest();
        req.setIdentification("777");
        req.setPassword("secreta1");

        when(rateLimitService.isUserBlocked("777")).thenReturn(false);
        when(userDetailsService.loadUserByUsername("777")).thenReturn(userDetails("777"));
        when(jwtUtil.generateToken(org.mockito.ArgumentMatchers.any())).thenReturn("jwt");
        when(personPartnerService.getByIdentification("777")).thenReturn(null);

        ResponseEntity<?> response = controller.createAuthenticationToken(req);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void changePassword_success_encodesNewPasswordAndClearsForceFlag() {
        UserDetails principal = userDetails("123");
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, Collections.emptyList()));

        PersonPartner partner = new PersonPartner();
        partner.setIdentification("123");
        partner.setForcePasswordChange(true);
        when(personPartnerService.getByIdentification("123")).thenReturn(partner);
        when(passwordEncoder.encode("nuevaClave1")).thenReturn("hash-nuevo");

        ChangePasswordRequest req = new ChangePasswordRequest();
        req.setNewPassword("nuevaClave1");

        ResponseEntity<?> response = controller.changePassword(req);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        org.mockito.ArgumentCaptor<PersonPartner> captor = forClass(PersonPartner.class);
        verify(personPartnerService).savePartner(captor.capture());
        assertEquals("hash-nuevo", captor.getValue().getPassword());
        assertTrue(!captor.getValue().getForcePasswordChange());
    }

    @Test
    void changePassword_returnsUnauthorized_whenNoAuthentication() {
        ChangePasswordRequest req = new ChangePasswordRequest();
        req.setNewPassword("nuevaClave1");

        ResponseEntity<?> response = controller.changePassword(req);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void login_success_recordsLoginSuccessEvent() {
        AuthRequest req = new AuthRequest();
        req.setIdentification("123");
        req.setPassword("secreta1");
        when(rateLimitService.isUserBlocked("123")).thenReturn(false);
        when(userDetailsService.loadUserByUsername("123")).thenReturn(userDetails("123"));
        when(jwtUtil.generateToken(org.mockito.ArgumentMatchers.any())).thenReturn("jwt");
        PersonPartner partner = new PersonPartner();
        partner.setIdentification("123");
        partner.setRole("ADMIN");
        partner.setForcePasswordChange(false);
        when(personPartnerService.getByIdentification("123")).thenReturn(partner);

        controller.createAuthenticationToken(req);

        verify(auditService).record(eq(co.unbosque.model.AuditEventType.LOGIN_SUCCESS),
                eq(co.unbosque.model.AuditResult.SUCCESS), eq("123"),
                org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.isNull());
    }

    @Test
    void login_badCredentials_recordsLoginFailedEvent() {
        AuthRequest req = new AuthRequest();
        req.setIdentification("123");
        req.setPassword("mala");
        when(rateLimitService.isUserBlocked("123")).thenReturn(false);
        org.mockito.Mockito.doThrow(new RuntimeException("bad")).when(authenticationManager)
                .authenticate(org.mockito.ArgumentMatchers.any());

        controller.createAuthenticationToken(req);

        verify(auditService).record(eq(co.unbosque.model.AuditEventType.LOGIN_FAILED),
                eq(co.unbosque.model.AuditResult.FAILURE), eq("123"),
                org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.isNull());
    }
}
