package co.edu.unbosque.controller;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;

import co.edu.unbosque.dto.AuthRequest;
import co.edu.unbosque.dto.AuthResponse;
import co.edu.unbosque.dto.ChangePasswordRequest;
import co.edu.unbosque.dto.ForgotPasswordRequest;
import co.edu.unbosque.dto.ResetPasswordRequest;
import co.edu.unbosque.model.AuditEventType;
import co.edu.unbosque.model.AuditResult;
import co.edu.unbosque.model.PasswordResetToken;
import co.edu.unbosque.model.PersonPartner;
import co.edu.unbosque.repository.PasswordResetTokenRepository;
import co.edu.unbosque.security.HttpRequestUtils;
import co.edu.unbosque.security.JwtUtil;
import co.edu.unbosque.security.PiiMasking;
import co.edu.unbosque.service.AuditService;
import co.edu.unbosque.service.EmailService;
import co.edu.unbosque.service.PersonPartnerService;
import co.edu.unbosque.service.RateLimitService;
import co.edu.unbosque.service.TokenBlacklistService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

	private final AuthenticationManager authenticationManager;
	private final UserDetailsService userDetailsService;
	private final JwtUtil jwtUtil;
	private final PersonPartnerService personPartnerService;
	private final PasswordEncoder passwordEncoder;
	private final PasswordResetTokenRepository tokenRepo;
	private final EmailService emailService;
	private final RateLimitService rateLimitService;
	private final TokenBlacklistService tokenBlacklistService;
	private final AuditService auditService;

	public static final String CURRENT_CONSENT_VERSION = co.edu.unbosque.config.ConsentPolicy.VERSION;

	public static boolean needsConsent(PersonPartner p) {
		return !Boolean.TRUE.equals(p.getConsentAccepted())
				|| !CURRENT_CONSENT_VERSION.equals(p.getConsentVersion());
	}

	public AuthController(AuthenticationManager authenticationManager, UserDetailsService userDetailsService,
			JwtUtil jwtUtil, PersonPartnerService personPartnerService, PasswordEncoder passwordEncoder,
			PasswordResetTokenRepository tokenRepo, EmailService emailService, RateLimitService rateLimitService,
			TokenBlacklistService tokenBlacklistService, AuditService auditService) {
		this.authenticationManager = authenticationManager;
		this.userDetailsService = userDetailsService;
		this.jwtUtil = jwtUtil;
		this.personPartnerService = personPartnerService;
		this.passwordEncoder = passwordEncoder;
		this.tokenRepo = tokenRepo;
		this.emailService = emailService;
		this.rateLimitService = rateLimitService;
		this.tokenBlacklistService = tokenBlacklistService;
		this.auditService = auditService;
	}

	@PostMapping("/login")
	public ResponseEntity<?> createAuthenticationToken(@Valid @RequestBody AuthRequest authRequest) {
		String ip = HttpRequestUtils.currentClientIp();
		if (rateLimitService.isUserBlocked(authRequest.getIdentification())) {
			auditService.record(AuditEventType.RATE_LIMIT_BLOCK, AuditResult.FAILURE,
					authRequest.getIdentification(), ip, "Bloqueo por intentos", null);
			return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
					.body("Demasiados intentos. Intente de nuevo más tarde.");
		}
		try {
			authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authRequest.getIdentification(),
					authRequest.getPassword()));
		} catch (Exception e) {
			rateLimitService.registerFailedLogin(authRequest.getIdentification());
			auditService.record(AuditEventType.LOGIN_FAILED, AuditResult.FAILURE,
					authRequest.getIdentification(), ip, "Credenciales incorrectas", null);
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciales incorrectas");
		}
		rateLimitService.resetUserFailures(authRequest.getIdentification());
		auditService.record(AuditEventType.LOGIN_SUCCESS, AuditResult.SUCCESS,
				authRequest.getIdentification(), ip, "Inicio de sesión", null);

		final UserDetails userDetails = userDetailsService.loadUserByUsername(authRequest.getIdentification());
		final String jwt = jwtUtil.generateToken(userDetails);
		PersonPartner partner = personPartnerService.getByIdentification(authRequest.getIdentification());
		if (partner == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
		}

		Boolean needsChange = partner.getForcePasswordChange();
		return ResponseEntity.ok(new AuthResponse(jwt, partner.getRole(),
				needsChange != null ? needsChange : true, needsConsent(partner)));
	}

	@PostMapping("/change-password")
	public ResponseEntity<?> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() instanceof String) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}

		String identification = ((UserDetails) auth.getPrincipal()).getUsername();
		PersonPartner titular = personPartnerService.getByIdentification(identification);
		if (titular == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
		}

		titular.setPassword(passwordEncoder.encode(request.getNewPassword()));
		titular.setForcePasswordChange(false);
		personPartnerService.savePartner(titular);

		auditService.record(AuditEventType.PASSWORD_CHANGED, AuditResult.SUCCESS,
				identification, HttpRequestUtils.currentClientIp(), "Cambio de contraseña", null);

		return ResponseEntity.ok("Contraseña cambiada exitosamente.");
	}

	@GetMapping("/consent")
	public ResponseEntity<?> getConsentPolicy() {
		return ResponseEntity.ok(new co.edu.unbosque.dto.ConsentPolicyDTO(
				co.edu.unbosque.config.ConsentPolicy.VERSION,
				co.edu.unbosque.config.ConsentPolicy.TITLE,
				co.edu.unbosque.config.ConsentPolicy.TEXT));
	}

	@PostMapping("/accept-consent")
	public ResponseEntity<?> acceptConsent() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() instanceof String) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		String identification = ((UserDetails) auth.getPrincipal()).getUsername();
		PersonPartner p = personPartnerService.getByIdentification(identification);
		if (p == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
		}
		p.setConsentAccepted(true);
		p.setConsentVersion(CURRENT_CONSENT_VERSION);
		p.setConsentAcceptedAt(LocalDateTime.now());
		personPartnerService.savePartner(p);
		auditService.record(AuditEventType.CONSENT_ACCEPTED, AuditResult.SUCCESS,
				identification, HttpRequestUtils.currentClientIp(), "Consentimiento v" + CURRENT_CONSENT_VERSION, null);
		return ResponseEntity.ok().build();
	}

	@PostMapping("/logout")
	public ResponseEntity<?> logout(HttpServletRequest request,
			@RequestParam(required = false) String reason) {
		String header = request.getHeader("Authorization");
		String username = null;
		if (header != null && header.startsWith("Bearer ")) {
			String jwt = header.substring(7);
			try {
				username = jwtUtil.extractUsername(jwt);
				String jti = jwtUtil.extractJti(jwt);
				Date expiration = jwtUtil.extractExpiration(jwt);
				PersonPartner revokedOwner = personPartnerService.getByIdentification(username);
				tokenBlacklistService.revoke(jti,
						expiration.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime(), revokedOwner);
				auditService.record(AuditEventType.TOKEN_REVOKED, AuditResult.SUCCESS,
						username, HttpRequestUtils.currentClientIp(), "Token revocado", null);
			} catch (Exception e) {
			}
		}
		String detail = "inactividad".equalsIgnoreCase(reason) ? "Cierre de sesión por inactividad" : "Cierre de sesión";
		auditService.record(AuditEventType.LOGOUT, AuditResult.SUCCESS, username,
				HttpRequestUtils.currentClientIp(), detail, null);
		return ResponseEntity.ok("Sesión cerrada.");
	}

	@PostMapping("/forgot-password")
	public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
		String email = request.getEmail();
		PersonPartner partner = personPartnerService.getByEmail(email);

		if (partner != null) {
			tokenRepo.deleteByPartner(partner);

			String token = UUID.randomUUID().toString();
			PasswordResetToken resetToken = new PasswordResetToken(token, partner, LocalDateTime.now().plusHours(1));
			tokenRepo.save(resetToken);

			try {
				emailService.sendPasswordResetEmail(email, token, partner.getFirstName());
			} catch (Exception e) {
				System.err.println("Error sending reset email: " + e.getMessage());
			}
		}

		auditService.record(AuditEventType.PASSWORD_RESET_REQUESTED, AuditResult.SUCCESS,
				PiiMasking.maskEmail(request.getEmail()), HttpRequestUtils.currentClientIp(),
				"Solicitud de recuperación", null);

		return ResponseEntity.ok("Si el correo existe en nuestro sistema, recibirás las instrucciones en breve.");
	}

	@PostMapping("/reset-password")
	public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
		PasswordResetToken resetToken = tokenRepo.findByToken(request.getToken()).orElse(null);

		if (resetToken == null || resetToken.isExpired()) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body("El enlace de recuperación es inválido o ha expirado.");
		}

		PersonPartner partner = resetToken.getPartner();
		partner.setPassword(passwordEncoder.encode(request.getNewPassword()));
		partner.setForcePasswordChange(false);
		personPartnerService.savePartner(partner);

		tokenRepo.delete(resetToken);

		auditService.record(AuditEventType.PASSWORD_RESET, AuditResult.SUCCESS,
				partner.getIdentification(), HttpRequestUtils.currentClientIp(), "Restablecimiento de contraseña", null);

		return ResponseEntity.ok("Contraseña restablecida exitosamente.");
	}
}
