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

/**
 * Endpoints de autenticación y gestión de la cuenta: sesión, contraseñas y consentimiento.
 *
 * <p>Es el controlador con más colaboradores del sistema —diez— porque concentra todo el ciclo de vida de la credencial:
 * emitir sesión, revocarla, cambiar la contraseña, recuperarla y registrar la aceptación de la política de datos.
 *
 * <h2>Advertencia: la protección de estos endpoints no está en la configuración</h2>
 *
 * <p>{@link co.edu.unbosque.security.SecurityConfig} declara <strong>todo</strong> {@code /auth/**} como
 * {@code permitAll()}. Sin embargo, {@link #changePassword} y {@link #acceptConsent} <strong>sí requieren sesión</strong>:
 * comprueban el principal a mano contra el contexto de seguridad y responden 401 si no hay ninguno.
 *
 * <p>El efecto es correcto, pero quien audite la seguridad leyendo únicamente la configuración concluirá, erróneamente,
 * que esas dos operaciones son anónimas.
 *
 * <h2>Formato de las respuestas</h2>
 *
 * <p>Este controlador devuelve <strong>cuerpos de texto plano</strong> en sus mensajes de éxito y error, no JSON. Es
 * relevante para el cliente: el frontend lee sus mensajes de error de un campo JSON, de modo que los textos redactados
 * aquí —incluido el aviso de bloqueo por intentos— se descartan y se sustituyen por textos genéricos del cliente.
 *
 * @see co.edu.unbosque.security.JwtUtil
 * @see co.edu.unbosque.service.TokenBlacklistService
 * @see co.edu.unbosque.config.ConsentPolicy
 */
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

	/** Versión vigente de la política de datos, contra la que se compara lo que cada socio aceptó. */
	public static final String CURRENT_CONSENT_VERSION = co.edu.unbosque.config.ConsentPolicy.VERSION;

	/**
	 * Indica si el socio debe aceptar la política de tratamiento de datos antes de usar la aplicación.
	 *
	 * <p>Devuelve {@code true} tanto si nunca aceptó como si aceptó <strong>una versión distinta de la vigente</strong>.
	 * Esa segunda condición es la que hace funcionar el versionado del consentimiento: publicar una versión nueva obliga a
	 * todos los socios a aceptarla de nuevo, en lugar de dar por válida una aceptación sobre un texto que ya cambió.
	 *
	 * <p>Nótese que el control es enteramente manual: el texto vive compilado en
	 * {@link co.edu.unbosque.config.ConsentPolicy}, de modo que editarlo <strong>sin incrementar la versión</strong> deja a
	 * todos los usuarios consintiendo un texto que nunca vieron.
	 *
	 * @param p socio a evaluar
	 * @return {@code true} si debe aceptar la política vigente
	 */
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

	/**
	 * Autentica al usuario y emite su JWT.
	 *
	 * <p>Secuencia: comprueba el bloqueo por intentos de la cuenta, autentica, registra el resultado en la bitácora y, si
	 * todo fue bien, emite el token y devuelve el estado inicial de la sesión.
	 *
	 * <p>Nótese que la limitación de tasa actúa en <strong>dos planos</strong>: por IP en
	 * {@link co.edu.unbosque.security.RateLimitFilter}, antes de llegar aquí, y por cuenta en este método. Ambos emiten
	 * 429, pero con formatos de cuerpo distintos —JSON el filtro, texto plano este método—, y el frontend solo interpreta
	 * el primero.
	 *
	 * <p>La respuesta lleva, además del token, los dos indicadores que el cliente necesita para saber si debe imponer un
	 * interstitial: si el usuario debe cambiar su contraseña y si debe aceptar la versión vigente de la política de datos.
	 *
	 * <p>Consideración de rendimiento: el socio se lee de la base <strong>tres veces</strong> en un inicio de sesión
	 * exitoso —al autenticar, al generar el token y al construir la respuesta—, y cada lectura descifra sus datos de
	 * contacto.
	 *
	 * @param authRequest identificación y contraseña
	 * @return {@code 200} con el token y el estado de la sesión; {@code 429} si la cuenta está bloqueada por intentos;
	 *         {@code 401} si las credenciales no son válidas; {@code 404} si la autenticación pasó pero el socio no se
	 *         pudo recuperar
	 */
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

	/**
	 * Cambia la contraseña del usuario autenticado y levanta la obligación de cambiarla.
	 *
	 * <p>La identidad se toma del contexto de seguridad, nunca del cuerpo de la petición, de modo que nadie puede cambiar
	 * la contraseña de otro.
	 *
	 * <p><strong>La comprobación de autenticación es manual.</strong> Como
	 * {@link co.edu.unbosque.security.SecurityConfig} declara todo {@code /auth/**} como público, este método verifica él
	 * mismo el principal y responde 401 si no hay ninguno. El efecto es correcto, pero no proviene de la configuración de
	 * seguridad.
	 *
	 * <p><strong>No exige la contraseña actual:</strong> el DTO solo lleva la nueva. Un token válido basta para
	 * establecerla, y la operación <strong>no revoca los JWT previos</strong> del usuario, que siguen siendo utilizables
	 * hasta su expiración.
	 *
	 * <p>La nueva contraseña se valida contra {@link co.edu.unbosque.validation.StrongPassword}; los incumplimientos los
	 * traduce {@link co.edu.unbosque.exception.GlobalExceptionHandler} a un 400 con el campo y su mensaje.
	 *
	 * @param request la contraseña nueva
	 * @return {@code 200} con un mensaje de confirmación; {@code 401} si no hay usuario autenticado; {@code 404} si el
	 *         socio no existe
	 */
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

	/**
	 * Devuelve la política vigente de tratamiento de datos personales: versión, título y texto.
	 *
	 * <p>Es genuinamente público —no comprueba el principal—, porque el texto legal debe poder leerse antes de aceptarlo.
	 *
	 * <p>El contenido proviene de {@link co.edu.unbosque.config.ConsentPolicy}, donde está compilado como constantes: el
	 * texto no se puede cambiar sin recompilar.
	 *
	 * @return {@code 200} con la política vigente
	 */
	@GetMapping("/consent")
	public ResponseEntity<?> getConsentPolicy() {
		return ResponseEntity.ok(new co.edu.unbosque.dto.ConsentPolicyDTO(
				co.edu.unbosque.config.ConsentPolicy.VERSION,
				co.edu.unbosque.config.ConsentPolicy.TITLE,
				co.edu.unbosque.config.ConsentPolicy.TEXT));
	}

	/**
	 * Registra que el usuario autenticado aceptó la versión vigente de la política de datos.
	 *
	 * <p>Almacena tres datos: que aceptó, <strong>qué versión</strong> aceptó y cuándo. Guardar la versión es lo que
	 * permite volver a pedir el consentimiento cuando la política cambie, en lugar de dar por válida una aceptación
	 * antigua sobre un texto distinto.
	 *
	 * <p>Como el resto de las operaciones de este controlador que requieren sesión, comprueba el principal a mano.
	 *
	 * @return {@code 200} sin cuerpo; {@code 401} si no hay usuario autenticado; {@code 404} si el socio no existe
	 */
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

	/**
	 * Cierra la sesión revocando el token presentado.
	 *
	 * <p>La revocación es lo que da efecto real al cierre de sesión: un JWT firmado seguiría siendo válido hasta expirar,
	 * así que su identificador se añade a la lista negra que
	 * {@link co.edu.unbosque.security.JwtAuthenticationFilter} consulta en cada petición.
	 *
	 * <p><strong>Responde 200 en cualquier circunstancia</strong>, incluso sin cabecera de autorización o con un token
	 * ilegible: el procesamiento del token va en un bloque que captura toda excepción. Es deliberado —un cliente que
	 * cierra sesión debe poder limpiar su estado local sin depender de que el servidor lo consiga—, pero implica que un
	 * 200 aquí no confirma que la revocación ocurriera.
	 *
	 * <p>Registra hasta dos eventos: la revocación del token, solo si esta tuvo lugar, y el cierre de sesión, siempre.
	 * Cuando el parámetro {@code reason} indica inactividad, el detalle lo refleja, lo que permite distinguir en la
	 * bitácora un cierre voluntario del automático por inactividad que aplica el cliente.
	 *
	 * @param request petición, de la que se lee la cabecera {@code Authorization}
	 * @param reason  motivo opcional del cierre; el valor {@code inactividad} se refleja en la bitácora
	 * @return {@code 200} con un mensaje de confirmación, siempre
	 */
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

	/**
	 * Inicia la recuperación de contraseña enviando un enlace al correo indicado.
	 *
	 * <p><strong>Responde exactamente lo mismo exista o no el correo.</strong> Es una decisión de seguridad deliberada: si
	 * la respuesta difiriera, el endpoint se convertiría en un oráculo para averiguar quién es socio del club. Por eso el
	 * mensaje está redactado en condicional y todo el trabajo va dentro de una comprobación silenciosa.
	 *
	 * <p>Los tokens anteriores del socio <strong>se eliminan</strong> antes de emitir el nuevo, de modo que solo un enlace
	 * está vigente a la vez. La vigencia es de una hora, calculada aquí y repetida —como texto— en la plantilla del
	 * correo.
	 *
	 * <p>El fallo de envío se captura y no altera la respuesta, para no delatar por esa vía la existencia del correo.
	 *
	 * <p>Es el único punto del sistema donde se aplica {@link co.edu.unbosque.security.PiiMasking}: el evento de auditoría
	 * guarda la dirección enmascarada. Todos los demás eventos almacenan la identificación en claro.
	 *
	 * <p>Consideraciones de costo: la búsqueda por correo <strong>recorre la tabla completa descifrando</strong>, y el
	 * envío del correo es <strong>sincrónico</strong>, de modo que la petición bloquea contra el servidor SMTP. Lo contiene
	 * una cubeta de limitación de tasa de tres solicitudes por hora.
	 *
	 * @param request el correo para el que se solicita la recuperación
	 * @return {@code 200} con un mensaje idéntico en todos los casos
	 */
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

	/**
	 * Establece una contraseña nueva a partir de un token de recuperación válido.
	 *
	 * <p>El token es la única credencial de esta operación: no hay sesión. De ahí que se compruebe tanto su existencia como
	 * su vigencia, y que <strong>se elimine tras usarse</strong>, haciéndolo de un solo uso.
	 *
	 * <p>Un token inexistente y uno caducado producen <strong>la misma respuesta</strong>, sin distinguirlos: no conviene
	 * confirmar a quien prueba tokens que acertó con uno que simplemente venció.
	 *
	 * <p>Además de fijar la contraseña, levanta la obligación de cambiarla, de modo que el socio entra directamente a la
	 * aplicación sin encontrarse un segundo interstitial.
	 *
	 * @param request el token y la contraseña nueva, validada contra la política de robustez
	 * @return {@code 200} con un mensaje de confirmación; {@code 400} si el token no existe o expiró
	 */
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
