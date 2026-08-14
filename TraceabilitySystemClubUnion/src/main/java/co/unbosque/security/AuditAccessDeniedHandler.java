package co.unbosque.security;

import java.io.IOException;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import co.unbosque.model.AuditEventType;
import co.unbosque.model.AuditResult;
import co.unbosque.service.AuditService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class AuditAccessDeniedHandler implements AccessDeniedHandler {

	private final AuditService auditService;

	public AuditAccessDeniedHandler(AuditService auditService) {
		this.auditService = auditService;
	}

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
