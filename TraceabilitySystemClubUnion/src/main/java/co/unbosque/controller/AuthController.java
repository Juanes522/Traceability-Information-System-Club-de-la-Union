package co.unbosque.controller;

import java.time.LocalDateTime;
import java.util.UUID;

import co.unbosque.dto.AuthRequest;
import co.unbosque.dto.AuthResponse;
import co.unbosque.dto.ChangePasswordRequest;
import co.unbosque.dto.ForgotPasswordRequest;
import co.unbosque.dto.ResetPasswordRequest;
import co.unbosque.model.PasswordResetToken;
import co.unbosque.model.PersonPartner;
import co.unbosque.repository.PasswordResetTokenRepository;
import co.unbosque.security.JwtUtil;
import co.unbosque.service.EmailService;
import co.unbosque.service.PersonPartnerService;
import co.unbosque.service.RateLimitService;

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

	public AuthController(AuthenticationManager authenticationManager, UserDetailsService userDetailsService,
			JwtUtil jwtUtil, PersonPartnerService personPartnerService, PasswordEncoder passwordEncoder,
			PasswordResetTokenRepository tokenRepo, EmailService emailService, RateLimitService rateLimitService) {
		this.authenticationManager = authenticationManager;
		this.userDetailsService = userDetailsService;
		this.jwtUtil = jwtUtil;
		this.personPartnerService = personPartnerService;
		this.passwordEncoder = passwordEncoder;
		this.tokenRepo = tokenRepo;
		this.emailService = emailService;
		this.rateLimitService = rateLimitService;
	}

	@PostMapping("/login")
	public ResponseEntity<?> createAuthenticationToken(@RequestBody AuthRequest authRequest) {
		if (rateLimitService.isUserBlocked(authRequest.getIdentification())) {
			return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
					.body("Demasiados intentos. Intente de nuevo más tarde.");
		}
		try {
			authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authRequest.getIdentification(),
					authRequest.getPassword()));
		} catch (Exception e) {
			rateLimitService.registerFailedLogin(authRequest.getIdentification());
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciales incorrectas");
		}
		rateLimitService.resetUserFailures(authRequest.getIdentification());

		final UserDetails userDetails = userDetailsService.loadUserByUsername(authRequest.getIdentification());
		final String jwt = jwtUtil.generateToken(userDetails);
		PersonPartner partner = personPartnerService.getByIdentification(authRequest.getIdentification());
		if (partner == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
		}

		Boolean needsChange = partner.getForcePasswordChange();
		return ResponseEntity.ok(new AuthResponse(jwt, partner.getRole(), needsChange != null ? needsChange : true));
	}

	@PostMapping("/change-password")
	public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest request) {
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

		return ResponseEntity.ok("Contraseña cambiada exitosamente.");
	}

	@PostMapping("/forgot-password")
	public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest request) {
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

		return ResponseEntity.ok("Si el correo existe en nuestro sistema, recibirás las instrucciones en breve.");
	}

	@PostMapping("/reset-password")
	public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
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

		return ResponseEntity.ok("Contraseña restablecida exitosamente.");
	}
}
