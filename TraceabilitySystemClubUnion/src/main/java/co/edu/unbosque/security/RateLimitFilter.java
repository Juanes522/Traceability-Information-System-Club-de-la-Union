package co.edu.unbosque.security;

import co.edu.unbosque.service.RateLimitService;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Limita la tasa de peticiones a los endpoints de autenticación, por dirección IP.
 *
 * <p>Es el <strong>primer</strong> filtro de la cadena, antes de {@link JwtAuthenticationFilter}: actúa
 * sobre tráfico todavía anónimo, que es exactamente el que hay que contener en un ataque de fuerza bruta
 * contra el inicio de sesión.
 *
 * <p>Aplica dos cubetas distintas, ambas gestionadas por
 * {@link co.edu.unbosque.service.RateLimitService}: una para {@code /auth/forgot-password}, más estricta
 * porque cada solicitud dispara un envío de correo y un recorrido completo de la tabla de socios, y otra
 * para el resto de {@code /auth/*}.
 *
 * <p>Nótese que existe un <strong>tercer</strong> nivel de limitación que este filtro no aplica: la cubeta
 * por nombre de usuario, que {@link co.edu.unbosque.controller.AuthController} consulta a mano. Las
 * propiedades {@code ratelimit.login.user.*} pertenecen a esa cubeta, no a este filtro, y la respuesta 429
 * que emite el controlador tiene un formato distinto —texto plano— del JSON que emite este filtro.
 *
 * @see co.edu.unbosque.service.RateLimitService
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {

	private final RateLimitService rateLimitService;
	/** Direcciones de proxy autorizadas a declarar la IP del cliente mediante {@code X-Forwarded-For}. */
	private final Set<String> trustedProxies;

	/**
	 * Crea el filtro con el servicio de cubetas y la lista de proxies de confianza.
	 *
	 * @param rateLimitService servicio que administra las cubetas
	 * @param trustedProxies lista de proxies de confianza separada por comas, de
	 *                         {@code ratelimit.trusted-proxies}. <strong>Está vacía por defecto</strong>, con
	 *                         la consecuencia descrita en {@link #doFilterInternal}. Los valores se recortan
	 *                         con {@code trim()}, a diferencia de lo que hace
	 *                         {@link SecurityConfig} con los orígenes CORS
	 */
	public RateLimitFilter(RateLimitService rateLimitService,
			@Value("${ratelimit.trusted-proxies:}") String trustedProxies) {
		this.rateLimitService = rateLimitService;
		this.trustedProxies = Arrays.stream(trustedProxies.split(","))
				.map(String::trim)
				.filter(s -> !s.isEmpty())
				.collect(Collectors.toSet());
	}

	/**
	 * Restringe la actuación del filtro a {@code /auth} y {@code /auth/*}, excluyendo las peticiones
	 * {@code OPTIONS}.
	 *
	 * <p>Excluir el preflight de CORS es necesario: de lo contrario el navegador consumiría cupo antes de
	 * enviar la petición real, y un formulario de login agotaría la cubeta al doble de velocidad.
	 *
	 * <p>Consecuencia de acotar por prefijo y no por ruta exacta: <strong>todo el árbol {@code /auth}
	 * comparte la misma cubeta</strong>. Un usuario que consulte legítimamente el endpoint de consentimiento
	 * o cierre sesión consume presupuesto de inicio de sesión.
	 *
	 * @param request petición entrante
	 * @return {@code true} cuando el filtro debe abstenerse
	 */
	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		String path = requestPath(request);
		boolean isAuthPath = path.equals("/auth") || path.startsWith("/auth/");
		return !isAuthPath || "OPTIONS".equalsIgnoreCase(request.getMethod());
	}

	/**
	 * Consume un token de la cubeta correspondiente y, si está agotada, corta la petición con 429.
	 *
	 * <p>A diferencia de {@link JwtAuthenticationFilter}, este filtro <strong>sí interrumpe</strong> la
	 * cadena: escribe la respuesta directamente, incluyendo una cabecera {@code Retry-After} calculada a
	 * partir del tiempo que falta para la recarga de la cubeta.
	 *
	 * <p>El cuerpo es JSON con la forma {@code {"message": ...}}. Nótese que la cubeta por usuario que aplica
	 * {@link co.edu.unbosque.controller.AuthController} responde a la misma condición lógica con un cuerpo de
	 * <strong>texto plano</strong>: son dos formatos distintos para el mismo error, y el frontend, que lee
	 * {@code error.message}, solo interpreta correctamente el de este filtro.
	 *
	 * @param request petición a un endpoint de autenticación
	 * @param response respuesta; se escribe aquí si la cubeta está agotada
	 * @param filterChain cadena, que solo se continúa si quedaba cupo
	 * @throws ServletException si falla un filtro posterior
	 * @throws IOException      si falla la escritura de la respuesta
	 */
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		String ip = resolveClientIp(request);
		ConsumptionProbe probe;
		if ("/auth/forgot-password".equals(requestPath(request))) {
			probe = rateLimitService.tryConsumeForgotPassword(ip);
		} else {
			probe = rateLimitService.tryConsumeLoginByIp(ip);
		}

		if (probe.isConsumed()) {
			filterChain.doFilter(request, response);
			return;
		}

		long retryAfterSeconds = Math.max(1, TimeUnit.NANOSECONDS.toSeconds(probe.getNanosToWaitForRefill()));
		response.setStatus(429);
		response.setHeader("Retry-After", String.valueOf(retryAfterSeconds));
		response.setContentType("application/json;charset=UTF-8");
		response.getWriter().write("{\"message\": \"Demasiados intentos. Intente de nuevo más tarde.\"}");
	}

	/**
	 * Devuelve la ruta de la petición relativa al contexto de la aplicación.
	 *
	 * <p>Descontar el {@code contextPath} es imprescindible dado que el artefacto es un WAR: desplegado en un
	 * Tomcat externo bajo un contexto, el URI absoluto no empieza por {@code /auth} y las comparaciones de
	 * ruta de este filtro fallarían, dejando el inicio de sesión sin limitación alguna.
	 *
	 * @param request petición entrante
	 * @return la ruta sin el prefijo de contexto
	 */
	private String requestPath(HttpServletRequest request) {
		return request.getRequestURI().substring(request.getContextPath().length());
	}

	/**
	 * Determina la IP contra la que se contabiliza la petición.
	 *
	 * <p><strong>Esta es la implementación correcta del problema:</strong> solo acepta la cabecera
	 * {@code X-Forwarded-For} si la dirección del par figura entre los proxies de confianza. Sin esa
	 * comprobación, cualquier cliente podría eludir la limitación rotando direcciones inventadas.
	 *
	 * <p>Dos advertencias sobre las consecuencias:
	 *
	 * <ul>
	 *   <li>Como {@code ratelimit.trusted-proxies} <strong>está vacía por defecto</strong>, detrás de
	 *       cualquier proxy inverso todas las peticiones se contabilizan contra la IP del proxy, y los límites
	 *       pasan a ser <strong>globales en lugar de por cliente</strong>. Es la sorpresa de producción más
	 *       probable del módulo de seguridad, y poblar esa propiedad es obligatorio en tal despliegue.</li>
	 *   <li>{@link HttpRequestUtils#currentClientIp()}, que alimenta la auditoría, resuelve este mismo dato
	 *       <strong>sin</strong> validar el origen. La misma petición se limita por su dirección real pero se
	 *       audita bajo una falsificable.</li>
	 * </ul>
	 *
	 * @param request petición entrante
	 * @return la IP del cliente, o la del socket si no procede confiar en la cabecera
	 */
	private String resolveClientIp(HttpServletRequest request) {
		try {
			if (trustedProxies.contains(request.getRemoteAddr())) {
				String forwarded = request.getHeader("X-Forwarded-For");
				if (forwarded != null && !forwarded.isBlank()) {
					return forwarded.split(",")[0].trim();
				}
			}
		} catch (Exception e) {
		}
		return request.getRemoteAddr();
	}
}
