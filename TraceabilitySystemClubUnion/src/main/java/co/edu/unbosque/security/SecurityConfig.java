package co.edu.unbosque.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Configuración central de Spring Security: cadena de filtros, CORS, cabeceras, codificación de
 * contraseñas y reglas de acceso por ruta.
 *
 * <p>{@code @EnableMethodSecurity} es lo que habilita las expresiones {@code @PreAuthorize} de los
 * controladores, de modo que la autorización de este sistema se decide en <strong>dos planos</strong>:
 * aquí se declara qué rutas son públicas, y en cada método de controlador qué rol puede invocarla.
 *
 * <h2>Advertencia de lectura: {@code /auth/**} es público, pero no anónimo</h2>
 *
 * <p>La regla {@code requestMatchers("/auth/**").permitAll()} incluye {@code /auth/change-password},
 * {@code /auth/logout} y {@code /auth/accept-consent}. <strong>Esas operaciones no son anónimas:</strong>
 * {@link co.edu.unbosque.controller.AuthController} comprueba el contexto de seguridad a mano en cada una
 * y responde 401 si no hay principal. El efecto es correcto, pero quien lea solo esta clase concluirá lo
 * contrario.
 *
 * <p>La documentación OpenAPI y Swagger UI también son públicas, <strong>sin guarda de perfil</strong>:
 * toda la superficie de la API y todos los esquemas de DTO son legibles sin autenticarse, en cualquier
 * entorno.
 *
 * @see JwtAuthenticationFilter
 * @see RateLimitFilter
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

	@Autowired
	private JwtAuthenticationFilter jwtAuthenticationFilter;

	@Autowired
	private RateLimitFilter rateLimitFilter;

	@Autowired
	private AuditAccessDeniedHandler auditAccessDeniedHandler;

	@org.springframework.beans.factory.annotation.Value("${app.frontend.url:http://localhost:4200}")
	private String frontendUrl;

	@org.springframework.beans.factory.annotation.Value("${app.cors.allowed-origins:}")
	private String extraCorsOrigins;

	/**
	 * Construye la lista de orígenes permitidos para CORS.
	 *
	 * <p>Se compone en tres tramos: cuatro orígenes de desarrollo fijos en el código, el valor de
	 * {@code app.frontend.url}, y los orígenes adicionales de {@code app.cors.allowed-origins} separados por
	 * comas.
	 *
	 * <p>Dos detalles que conviene conocer antes de configurar un despliegue:
	 *
	 * <ul>
	 *   <li>Con la configuración por defecto {@code http://localhost:4200} aparece <strong>dos veces</strong>,
	 *       por estar tanto entre los fijos como en {@code app.frontend.url}. Es inocuo.</li>
	 *   <li>La división por comas <strong>no aplica {@code trim()}</strong>, de modo que
	 *       {@code "a.com, b.com"} produce un segundo origen inválido con un espacio inicial. Los valores
	 *       deben escribirse sin espacios. Nótese que {@link RateLimitFilter} sí recorta los suyos: las dos
	 *       clases del mismo paquete difieren en esto.</li>
	 * </ul>
	 *
	 * <p>{@code app.cors.allowed-origins} es un punto de extensión real que <strong>no está declarado en
	 * ningún archivo de propiedades</strong>, así que no se descubre leyendo la configuración.
	 *
	 * @return los orígenes permitidos, en orden de precedencia de composición
	 */
	private java.util.List<String> corsAllowedOrigins() {
		java.util.List<String> origins = new java.util.ArrayList<>(
				Arrays.asList("http://localhost:4200", "http://localhost:8080", "http://127.0.0.1:8080", "http://localhost:8000"));
		if (frontendUrl != null && !frontendUrl.isBlank()) {
			origins.add(frontendUrl);
		}
		if (extraCorsOrigins != null && !extraCorsOrigins.isBlank()) {
			origins.addAll(Arrays.asList(extraCorsOrigins.split(",")));
		}
		return origins;
	}

	/**
	 * Codificador de contraseñas con <strong>rampa de compatibilidad para contraseñas heredadas en texto
	 * plano</strong>.
	 *
	 * <p>Este bean es lo más importante que documentar de esta clase, porque su comportamiento no es el que
	 * su nombre sugiere. Codifica siempre con BCrypt, produciendo {@code {bcrypt}$2a$...}, pero
	 * {@code setDefaultPasswordEncoderForMatches(NoOpPasswordEncoder)} hace que cualquier valor almacenado
	 * <strong>sin</strong> prefijo {@code {id}} se compare <strong>como texto plano</strong>.
	 *
	 * <p>Es una decisión deliberada, no un descuido: permite que los socios cuya contraseña se guardó sin
	 * hash antes de introducir BCrypt sigan pudiendo entrar. {@code PasswordEncoderConfigTest} fija ese
	 * comportamiento de forma explícita, de modo que «corregirlo» sin más rompería la autenticación de esas
	 * filas y haría fallar las pruebas.
	 *
	 * <p>El reencriptado es automático: {@link UserDetailsServiceImpl} implementa también
	 * {@code UserDetailsPasswordService}, así que {@code DaoAuthenticationProvider} llama a
	 * {@code updatePassword} en el primer inicio de sesión exitoso de cada contraseña heredada.
	 *
	 * <h4>Lo que hay que saber para mantener esto</h4>
	 *
	 * <ul>
	 *   <li>Una contraseña en texto plano de un socio que <strong>nunca vuelva a entrar</strong> seguirá
	 *       autenticando indefinidamente.</li>
	 *   <li>{@code BCryptPasswordEncoder} usa la fuerza por defecto, 10, sin configurar.</li>
	 *   <li><strong>No existe mecanismo de cierre de la rampa:</strong> ni propiedad, ni indicador, ni fecha
	 *       límite. Retirar el respaldo exige editar este método, y nada informa de cuándo sería seguro
	 *       hacerlo. Un recuento de filas cuya contraseña no empiece por {@code {bcrypt}} sería la señal.</li>
	 *   <li>El reencriptado dispara un guardado de la entidad completa, y es por eso que el script de
	 *       migración de anchos de columna es obligatorio en bases preexistentes.</li>
	 * </ul>
	 *
	 * @return un {@code DelegatingPasswordEncoder} que codifica con BCrypt y acepta texto plano al comparar
	 */
	@Bean
	@SuppressWarnings("deprecation")
	public PasswordEncoder passwordEncoder() {
		Map<String, PasswordEncoder> encoders = new HashMap<>();
		encoders.put("bcrypt", new BCryptPasswordEncoder());
		DelegatingPasswordEncoder delegating = new DelegatingPasswordEncoder("bcrypt", encoders);
		delegating.setDefaultPasswordEncoderForMatches(
				org.springframework.security.crypto.password.NoOpPasswordEncoder.getInstance());
		return delegating;
	}

	/**
	 * Expone el {@code AuthenticationManager} autoconfigurado para que
	 * {@link co.edu.unbosque.controller.AuthController} pueda inyectarlo.
	 *
	 * <p>No se declara ningún {@code DaoAuthenticationProvider} explícito: Spring Boot lo compone a partir de
	 * {@link UserDetailsServiceImpl}, del {@link #passwordEncoder()} y del
	 * {@code UserDetailsPasswordService} que el primero implementa. Es precisamente esa composición implícita
	 * la que activa el reencriptado automático de contraseñas heredadas.
	 *
	 * @param authenticationConfiguration configuración de autenticación provista por el framework
	 * @return el gestor de autenticación
	 * @throws Exception si el framework no puede construirlo
	 */
	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
			throws Exception {
		return authenticationConfiguration.getAuthenticationManager();
	}

	/**
	 * Define la cadena de filtros de seguridad: CORS, cabeceras, política de sesión y reglas de acceso.
	 *
	 * <h4>Orden de los filtros</h4>
	 *
	 * <p>Las dos llamadas a {@code addFilterBefore} producen el orden
	 * {@code RateLimitFilter → JwtAuthenticationFilter → UsernamePasswordAuthenticationFilter}. Que la
	 * limitación de tasa vaya <strong>antes</strong> de toda autenticación es el objetivo: así protege el
	 * inicio de sesión de una avalancha anónima.
	 *
	 * <p>Nótese que la segunda llamada se posiciona respecto a la clase concreta
	 * {@code JwtAuthenticationFilter.class}, lo cual funciona solo porque la primera ya colocó ese filtro en
	 * la cadena. Es un modismo frágil: invertir el orden de las dos líneas rompería el registro.
	 *
	 * <h4>Reglas de acceso, en orden de evaluación</h4>
	 *
	 * <ol>
	 *   <li>{@code /auth/**} — público (véase la advertencia en la documentación de la clase)</li>
	 *   <li>{@code /v3/api-docs/**}, {@code /swagger-ui/**}, {@code /swagger-ui.html} — público</li>
	 *   <li>{@code /push/vapid-public-key} — público, porque el navegador la necesita antes de suscribirse</li>
	 *   <li>todo lo demás — autenticado, y acotado por {@code @PreAuthorize} en cada controlador</li>
	 * </ol>
	 *
	 * <h4>Cabeceras y sesión</h4>
	 *
	 * <p>Sesión {@code STATELESS}, CSRF deshabilitado —coherente con un API sin cookies—, CSP restrictiva,
	 * {@code X-Frame-Options: DENY} (redundante con {@code frame-ancestors 'none'}, a propósito) y HSTS de un
	 * año con subdominios.
	 *
	 * <p>Conviene matizar el {@code STATELESS}: describe que no se crea sesión de servlet, pero el camino de
	 * la petición <strong>sí consulta estado</strong>, dos veces por petición autenticada (el usuario y la
	 * lista negra de tokens).
	 *
	 * <p>Se registra un {@code accessDeniedHandler} para auditar las denegaciones, pero
	 * <strong>no un {@code authenticationEntryPoint}</strong>, de modo que el acceso no autenticado a un
	 * recurso protegido no queda registrado.
	 *
	 * @param http constructor de la cadena de seguridad
	 * @return la cadena configurada
	 * @throws Exception si la configuración es inválida
	 */
	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http.cors(cors -> cors.configurationSource(request -> {
			CorsConfiguration config = new CorsConfiguration();
			config.setAllowedOriginPatterns(corsAllowedOrigins()); 
																												
																												
																												
			config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
			config.setAllowedHeaders(
					Arrays.asList("Authorization", "Content-Type", "Accept", "Origin", "X-Requested-With"));
			config.setAllowCredentials(true);
			return config;
		})).csrf(csrf -> csrf.disable())
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.headers(headers -> headers
						.contentSecurityPolicy(csp -> csp.policyDirectives(
								"default-src 'self'; frame-ancestors 'none'; object-src 'none'; base-uri 'self'"))
						.frameOptions(frame -> frame.deny())
						.httpStrictTransportSecurity(hsts -> hsts
								.includeSubDomains(true)
								.maxAgeInSeconds(31536000)))
				.exceptionHandling(ex -> ex.accessDeniedHandler(auditAccessDeniedHandler))
				.authorizeHttpRequests(authz -> authz
						
						.requestMatchers("/auth/**").permitAll()
						.requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
						.requestMatchers("/push/vapid-public-key").permitAll()
						
						.anyRequest().authenticated());

		http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
		http.addFilterBefore(rateLimitFilter, JwtAuthenticationFilter.class);

		return http.build();
	}
}