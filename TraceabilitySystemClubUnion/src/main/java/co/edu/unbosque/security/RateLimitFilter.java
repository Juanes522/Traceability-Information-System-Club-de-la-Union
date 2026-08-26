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

@Component
public class RateLimitFilter extends OncePerRequestFilter {

	private final RateLimitService rateLimitService;
	private final Set<String> trustedProxies;

	public RateLimitFilter(RateLimitService rateLimitService,
			@Value("${ratelimit.trusted-proxies:}") String trustedProxies) {
		this.rateLimitService = rateLimitService;
		this.trustedProxies = Arrays.stream(trustedProxies.split(","))
				.map(String::trim)
				.filter(s -> !s.isEmpty())
				.collect(Collectors.toSet());
	}

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		String path = requestPath(request);
		boolean isAuthPath = path.equals("/auth") || path.startsWith("/auth/");
		return !isAuthPath || "OPTIONS".equalsIgnoreCase(request.getMethod());
	}

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

	private String requestPath(HttpServletRequest request) {
		return request.getRequestURI().substring(request.getContextPath().length());
	}

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
