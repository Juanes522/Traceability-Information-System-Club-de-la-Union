package co.edu.unbosque.controller;

import co.edu.unbosque.dto.AuthRequest;
import co.edu.unbosque.dto.AuthResponse;
import co.edu.unbosque.dto.ChangePasswordRequest;
import co.edu.unbosque.model.PersonPartner;
import co.edu.unbosque.repository.PasswordResetTokenRepository;
import co.edu.unbosque.security.JwtUtil;
import co.edu.unbosque.service.EmailService;
import co.edu.unbosque.service.PersonPartnerService;
import co.edu.unbosque.service.RateLimitService;
import co.edu.unbosque.service.TokenBlacklistService;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
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
    private co.edu.unbosque.service.AuditService auditService;
    private AuthController controller;

    @BeforeEach
    void setUp() {
        authenticationManager = mock(AuthenticationManager.class);
        userDetailsService = mock(UserDetailsService.class);
        jwtUtil = mock(JwtUtil.class);
        personPartnerService = mock(PersonPartnerService.class);
        passwordEncoder = mock(PasswordEncoder.class);
        rateLimitService = mock(RateLimitService.class);
        auditService = mock(co.edu.unbosque.service.AuditService.class);
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

        verify(auditService).record(eq(co.edu.unbosque.model.AuditEventType.LOGIN_SUCCESS),
                eq(co.edu.unbosque.model.AuditResult.SUCCESS), eq("123"),
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

        verify(auditService).record(eq(co.edu.unbosque.model.AuditEventType.LOGIN_FAILED),
                eq(co.edu.unbosque.model.AuditResult.FAILURE), eq("123"),
                org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.isNull());
    }

    @Test
    void needsConsent_trueSiNoAceptoOVersionDistinta() {
        PersonPartner p = new PersonPartner();
        assertThat(AuthController.needsConsent(p)).isTrue();
        p.setConsentAccepted(true);
        p.setConsentVersion("0.9");
        assertThat(AuthController.needsConsent(p)).isTrue();
        p.setConsentVersion(AuthController.CURRENT_CONSENT_VERSION);
        assertThat(AuthController.needsConsent(p)).isFalse();
    }

    @Test
    void acceptConsent_persisteYAudita() {
        UserDetails principal = userDetails("0001");
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, Collections.emptyList()));

        PersonPartner p = new PersonPartner();
        p.setIdentification("0001");
        when(personPartnerService.getByIdentification("0001")).thenReturn(p);

        ResponseEntity<?> resp = controller.acceptConsent();

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertThat(p.getConsentAccepted()).isTrue();
        assertThat(p.getConsentVersion()).isEqualTo(AuthController.CURRENT_CONSENT_VERSION);
        assertThat(p.getConsentAcceptedAt()).isNotNull();
        verify(personPartnerService).savePartner(p);
        verify(auditService).record(eq(co.edu.unbosque.model.AuditEventType.CONSENT_ACCEPTED),
                eq(co.edu.unbosque.model.AuditResult.SUCCESS), eq("0001"), any(), any(), isNull());
    }

    @Test
    void acceptConsent_returnsUnauthorized_whenNoAuthentication() {
        ResponseEntity<?> resp = controller.acceptConsent();

        assertEquals(HttpStatus.UNAUTHORIZED, resp.getStatusCode());
    }

    @Test
    void logout_conReasonInactividad_auditaDetalleDeInactividad() {
        jakarta.servlet.http.HttpServletRequest req = new org.springframework.mock.web.MockHttpServletRequest();
        controller.logout(req, "inactividad");
        verify(auditService).record(eq(co.edu.unbosque.model.AuditEventType.LOGOUT),
                eq(co.edu.unbosque.model.AuditResult.SUCCESS), any(), any(),
                eq("Cierre de sesión por inactividad"), isNull());
    }

    @Test
    void logout_sinReason_auditaCierreNormal() {
        jakarta.servlet.http.HttpServletRequest req = new org.springframework.mock.web.MockHttpServletRequest();
        controller.logout(req, null);
        verify(auditService).record(eq(co.edu.unbosque.model.AuditEventType.LOGOUT),
                eq(co.edu.unbosque.model.AuditResult.SUCCESS), any(), any(),
                eq("Cierre de sesión"), isNull());
    }

    @Test
    void getConsentPolicy_returnsCurrentVersionTitleAndText() {
        ResponseEntity<?> resp = controller.getConsentPolicy();

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        co.edu.unbosque.dto.ConsentPolicyDTO body = (co.edu.unbosque.dto.ConsentPolicyDTO) resp.getBody();
        assertEquals(AuthController.CURRENT_CONSENT_VERSION, body.getVersion());
        assertThat(body.getTitle()).isNotBlank();
        assertThat(body.getText()).isNotBlank();
    }
}
