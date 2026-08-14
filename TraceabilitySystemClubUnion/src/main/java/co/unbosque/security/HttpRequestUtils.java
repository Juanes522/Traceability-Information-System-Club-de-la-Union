package co.unbosque.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public final class HttpRequestUtils {

	private HttpRequestUtils() {
	}

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
