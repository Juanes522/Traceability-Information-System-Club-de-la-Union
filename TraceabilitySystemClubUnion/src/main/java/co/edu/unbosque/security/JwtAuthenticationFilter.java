package co.edu.unbosque.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import co.edu.unbosque.service.TokenBlacklistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Autentica cada petición a partir del JWT de la cabecera {@code Authorization}.
 *
 * <p>Se registra en la cadena antes de {@code UsernamePasswordAuthenticationFilter}, y después de
 * {@link RateLimitFilter}.
 *
 * <p>Nota sobre el registro del filtro: al estar anotado {@code @Component}, Spring Boot lo registra
 * <strong>también</strong> como filtro de servlet para todas las URL, además de dentro de la cadena de
 * seguridad. El comportamiento sigue siendo correcto porque {@code OncePerRequestFilter} se protege de la
 * doble ejecución dentro de un mismo despacho, pero conviene saber que el filtro se ejecuta fuera del
 * ámbito de rutas de la cadena. Lo mismo aplica a {@link RateLimitFilter}.
 *
 * @see JwtUtil
 * @see co.edu.unbosque.service.TokenBlacklistService
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private TokenBlacklistService tokenBlacklist;

    /**
     * Valida el token de la cabecera {@code Authorization} y, si procede, puebla el contexto de seguridad.
     *
     * <p>Dos condiciones deben cumplirse para autenticar: que el token sea íntegro y del usuario
     * ({@link JwtUtil#validateToken}) y que <strong>no figure en la lista negra</strong> de revocación.
     *
     * <h4>Nunca interrumpe la petición</h4>
     *
     * <p>Si el token falta, está expirado, manipulado o revocado, el filtro <strong>no devuelve 401</strong>:
     * deja el contexto de seguridad vacío y continúa la cadena, delegando el rechazo en la capa de
     * autorización. Es deliberado —así las rutas públicas siguen funcionando sin token—, pero implica que el
     * código de estado ante un token inválido lo decide Spring Security, no este filtro.
     *
     * <h4>Los tokens inválidos no dejan rastro</h4>
     *
     * <p>La extracción del usuario está envuelta en un {@code catch} de cuerpo vacío, de modo que
     * <strong>un token expirado, manipulado o malformado es indistinguible de la ausencia de token</strong>
     * tanto en los logs como en la bitácora de auditoría. No hay evento de auditoría para un intento de
     * autenticación fallido por token.
     *
     * <h4>Costo por petición</h4>
     *
     * <p>Cada petición autenticada provoca <strong>dos consultas</strong> a la base: la carga del usuario
     * —necesaria porque el token no lleva claim de rol— y la comprobación de la lista negra, ninguna con
     * caché. Nótese además que el usuario se carga <strong>antes</strong> de validar el token, de modo que
     * incluso un token expirado o revocado cuesta esa consulta.
     *
     * @param request petición entrante
     * @param response respuesta; este filtro no escribe en ella
     * @param filterChain cadena a continuar, siempre invocada
     * @throws ServletException si falla un filtro posterior
     * @throws IOException      si falla la entrada/salida de un filtro posterior
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        final String authorizationHeader = request.getHeader("Authorization");

        String username = null;
        String jwt = null;

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            try {
                username = jwtUtil.extractUsername(jwt);
            } catch (Exception e) {
                // Invalid or expired token
            }
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

            if (jwtUtil.validateToken(jwt, userDetails) && !tokenBlacklist.isRevoked(jwtUtil.extractJti(jwt))) {
                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                
                usernamePasswordAuthenticationToken
                        .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                
                SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
            }
        }
        filterChain.doFilter(request, response);
    }
}
