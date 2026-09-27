package co.edu.unbosque.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Utilidad para obtener la dirección IP del cliente desde código que no recibe la petición como
 * parámetro.
 *
 * <p>Se apoya en {@code RequestContextHolder}, lo que permite invocarla desde un servicio o un manejador
 * sin propagar el {@code HttpServletRequest} por toda la pila de llamadas.
 *
 * @see RateLimitFilter
 */
public final class HttpRequestUtils {

	private HttpRequestUtils() {
	}

	/**
	 * Devuelve la IP atribuida al cliente de la petición en curso.
	 *
	 * <p>Prefiere el primer salto de la cabecera {@code X-Forwarded-For} y, en su ausencia, la dirección del
	 * socket.
	 *
	 * <h4>Advertencia: esta resolución es falsificable</h4>
	 *
	 * <p><strong>Confía en {@code X-Forwarded-For} sin condición alguna</strong>, de modo que cualquier
	 * cliente puede elegir la dirección con la que quedará registrado enviando esa cabecera.
	 *
	 * <p>La contradicción con {@link RateLimitFilter#doFilterInternal} es directa y conviene conocerla: ese
	 * filtro resuelve el mismo dato <em>correctamente</em>, honrando la cabecera solo si el par figura en una
	 * lista blanca de proxies de confianza. Como esta utilidad es la que alimenta toda la auditoría, la misma
	 * petición se limita por su dirección real pero <strong>se audita bajo una dirección que el atacante
	 * elige</strong>. La bitácora no debe tratarse como prueba de origen.
	 *
	 * <h4>Uso desde código asíncrono</h4>
	 *
	 * <p>{@code RequestContextHolder} es un {@code ThreadLocal} que <strong>no se propaga</strong> a los
	 * hilos de {@code @Async} ni a los trabajos programados. Por eso todos los puntos de llamada resuelven
	 * la IP en el hilo de la petición y la pasan como parámetro a
	 * {@link co.edu.unbosque.service.AuditService}, en lugar de dejar que el método asíncrono la consulte.
	 * Invertir ese orden devolvería siempre {@code null}.
	 *
	 * @return la IP atribuida al cliente, o {@code null} si no hay petición en curso o si la resolución falla
	 */
	public static String currentClientIp() {
		try {
			ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
			if (attrs == null) {
				return null;
			}
			HttpServletRequest request = attrs.getRequest();
			String forwarded = request.getHeader("X-Forwarded-For");
			if (forwarded != null && !forwarded.isBlank()) {
				return forwarded.split(",")[0].trim();
			}
			return request.getRemoteAddr();
		} catch (Exception e) {
			return null;
		}
	}
}
