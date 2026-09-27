package co.edu.unbosque.security;

import java.io.IOException;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import co.edu.unbosque.model.AuditEventType;
import co.edu.unbosque.model.AuditResult;
import co.edu.unbosque.service.AuditService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Registra en la bitácora los intentos de acceso rechazados por falta de permisos, y responde 403.
 *
 * <p>Se conecta a la cadena de seguridad mediante
 * {@code exceptionHandling(ex -> ex.accessDeniedHandler(...))} en {@link SecurityConfig}, y atiende tanto
 * las denegaciones de la cadena como las que lanzan las expresiones {@code @PreAuthorize} de los
 * controladores.
 *
 * <h2>Cubre solo la mitad del problema</h2>
 *
 * <p>{@link SecurityConfig} <strong>no registra un {@code AuthenticationEntryPoint}</strong> equivalente,
 * de modo que este manejador audita los fallos de <em>autorización</em> —un usuario identificado que
 * intenta algo que no le corresponde— pero el acceso <em>no autenticado</em> a un recurso protegido no se
 * registra en ninguna parte. Discutiblemente, esa segunda señal es la más interesante de las dos.
 *
 * @see co.edu.unbosque.model.AuditEventType#ACCESS_DENIED
 */
@Component
public class AuditAccessDeniedHandler implements AccessDeniedHandler {

	private final AuditService auditService;

	public AuditAccessDeniedHandler(AuditService auditService) {
		this.auditService = auditService;
	}

	/**
	 * Audita la denegación y devuelve 403.
	 *
	 * <p>El evento se clasifica como {@code ACCESS_DENIED}, al que
	 * {@link co.edu.unbosque.service.AuditService} asigna severidad {@code CRITICAL}.
	 *
	 * <p>Tres consideraciones sobre el comportamiento resultante:
	 *
	 * <ul>
	 *   <li>El registro es {@code @Async} y traga sus excepciones, así que <strong>si Elasticsearch no está
	 *       disponible este evento crítico se pierde en silencio</strong>, con una sola línea en
	 *       {@code System.err} como rastro.</li>
	 *   <li>{@code sendError(403)} delega en la página de error del contenedor, donde surten efecto las
	 *       propiedades {@code server.error.include-*=never}: el cliente recibe un cuerpo sin mensaje ni
	 *       traza. Es endurecimiento coherente y deliberado.</li>
	 *   <li>El URI solicitado se concatena al detalle del evento <strong>sin sanear</strong>. Este backend
	 *       no lo renderiza, pero cualquier consumidor que muestre la bitácora como HTML debe escaparlo para
	 *       no exponerse a una inyección a través de una ruta construida a propósito.</li>
	 * </ul>
	 *
	 * @param request               petición rechazada; de ella se toma el URI para el detalle del evento
	 * @param response              respuesta en la que se escribe el 403
	 * @param accessDeniedException excepción que originó el rechazo; no se inspecciona
	 * @throws IOException si falla la escritura de la respuesta de error
	 */
	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response,
			AccessDeniedException accessDeniedException) throws IOException {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String username = auth != null ? auth.getName() : null;
		auditService.record(AuditEventType.ACCESS_DENIED, AuditResult.FAILURE, username,
				HttpRequestUtils.currentClientIp(), "Acceso denegado: " + request.getRequestURI(), null);
		response.sendError(HttpServletResponse.SC_FORBIDDEN);
	}
}
