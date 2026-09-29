package co.edu.unbosque.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

/**
 * Emisión y verificación de los JWT de acceso.
 *
 * <h2>Conjunto de claims</h2>
 *
 * <p>Es deliberadamente mínimo: <strong>{@code jti}, {@code sub}, {@code iat} y {@code exp}, y nada
 * más</strong>.
 *
 * <ul>
 *   <li>{@code jti} — UUID aleatorio. Es lo que hace posible la revocación: sin él,
 *       {@link co.edu.unbosque.service.TokenBlacklistService} no tendría nada que anotar.</li>
 *   <li>{@code sub} — la {@code identification} del socio, que es el nombre de usuario del sistema.</li>
 *   <li><strong>No hay claim de rol.</strong> Es la decisión de diseño más consecuente de esta clase:
 *       obliga a {@link JwtAuthenticationFilter} a resolver las autoridades contra la base en cada
 *       petición, con lo que revocar o degradar un privilegio surte efecto de inmediato, al precio de una
 *       consulta por petición.</li>
 *   <li><strong>No hay {@code iss}, {@code aud} ni {@code nbf}</strong>, de modo que el verificador no
 *       comprueba emisor ni audiencia.</li>
 * </ul>
 *
 * <p>Tampoco existe token de refresco: el token de acceso de ocho horas constituye toda la sesión.
 *
 * <h2>Clave de firma</h2>
 *
 * <p>Se deriva de {@code ${jwt.secret}}, inyectada sin valor por defecto. No se valida su longitud en este
 * código: si la clave tiene menos de 256 bits, JJWT lanza {@code WeakKeyException} al crear el bean, un
 * mensaje bastante menos claro que el que produce {@link AesGcmEncryptionService} en el caso equivalente.
 *
 * <p>Nótese que el proyecto fija JJWT 0.11.5, cuya API de constructores ({@code parserBuilder},
 * {@code setClaims}, {@code signWith(key, alg)}) quedó obsoleta o desapareció en 0.12.x. El código es
 * internamente coherente, pero no se puede actualizar la biblioteca sin reescribir esta clase.
 */
@Component
public class JwtUtil {

    private final SecretKey secretKey;

    /**
     * Crea la utilidad derivando la clave de firma del secreto configurado.
     *
     * @param secret secreto de firma HMAC, de {@code ${jwt.secret}}. Debe tener al menos 32 bytes; por
     *               debajo de ese tamaño la creación del bean falla con una excepción de JJWT
     */
    public JwtUtil(@Value("${jwt.secret}") String secret) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Vigencia del token: ocho horas.
     *
     * <p><strong>Es una constante de código, no una propiedad configurable.</strong> Ajustar la duración de
     * la sesión exige recompilar.
     */
    // 8 hours
    private final long jwtExpirationMs = 8 * 60 * 60 * 1000;

    /**
     * Extrae la identificación del socio del claim {@code sub}.
     *
     * @param token JWT a inspeccionar
     * @return la identificación
     * @throws io.jsonwebtoken.JwtException si la firma no es válida, el token está malformado o ya expiró
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extrae el identificador único del token (claim {@code jti}).
     *
     * <p>Es el valor con el que se consulta y se alimenta la lista negra de tokens revocados.
     *
     * @param token JWT a inspeccionar
     * @return el {@code jti}
     * @throws io.jsonwebtoken.JwtException si el token no es válido
     */
    public String extractJti(String token) {
        return extractClaim(token, Claims::getId);
    }

    /**
     * Extrae la fecha de expiración del token.
     *
     * <p>La usa el cierre de sesión para saber hasta cuándo debe mantenerse la revocación en la lista negra.
     *
     * @param token JWT a inspeccionar
     * @return la fecha de expiración
     * @throws io.jsonwebtoken.JwtException si el token no es válido
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extrae un claim arbitrario aplicando la función indicada.
     *
     * @param <T> tipo del valor extraído
     * @param token JWT a inspeccionar
     * @param claimsResolver función que selecciona el claim deseado
     * @return el valor del claim
     * @throws io.jsonwebtoken.JwtException si el token no es válido
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Verifica la firma y devuelve el cuerpo del token.
     *
     * <p><strong>Aquí es donde se comprueba realmente la firma</strong>, y donde se rechaza un token
     * manipulado o expirado mediante excepción. Todos los métodos de extracción pasan por este punto, de
     * modo que ninguno puede leer claims de un token no verificado.
     *
     * @param token JWT a verificar
     * @return los claims verificados
     * @throws io.jsonwebtoken.JwtException si la firma no coincide, el token está malformado o ya expiró
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token).getBody();
    }

    /**
     * Indica si el token ya expiró.
     *
     * @param token JWT a inspeccionar
     * @return {@code true} si la expiración es anterior al instante actual
     */
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Emite un token para un usuario ya autenticado.
     *
     * <p>Nótese que construye un mapa de claims <strong>vacío</strong> y lo pasa a
     * {@link #createToken(Map, String)}: no se añade ninguna información adicional al token, en particular
     * ningún rol ni autoridad. Es intencional, y es lo que obliga a resolver los permisos contra la base en
     * cada petición.
     *
     * @param userDetails usuario autenticado; solo se usa su nombre de usuario
     * @return el JWT firmado, con vigencia de ocho horas
     */
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, userDetails.getUsername());
    }

    /**
     * Construye y firma el token.
     *
     * <p>El parámetro {@code claims} es, en la práctica, <strong>código muerto</strong>: su único invocador
     * le pasa siempre un mapa vacío, y los imports de {@code Map} y {@code HashMap} existen solo por él.
     *
     * <p>Conviene además conocer una trampa de JJWT 0.11.x presente aquí: {@code setClaims(...)}
     * <strong>reemplaza</strong> el mapa de claims completo en lugar de añadir. Funciona porque se invoca
     * antes de {@code setId} y {@code setSubject}; invertir ese orden borraría silenciosamente el {@code jti}
     * y el {@code sub}, y con ello la capacidad de revocar tokens.
     *
     * @param claims claims adicionales; hoy siempre vacío
     * @param subject identificación del socio, que será el claim {@code sub}
     * @return el JWT firmado con HS256
     */
    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setId(UUID.randomUUID().toString())
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Comprueba que el token corresponde al usuario indicado y que no ha expirado.
     *
     * <p>No verifica la firma de forma explícita: se apoya en que {@link #extractUsername(String)} lanza
     * excepción si el token no es íntegro. Como consecuencia, este método <strong>solo es seguro si se
     * invoca sobre un token no analizado previamente</strong>, que es como lo usa
     * {@link JwtAuthenticationFilter}.
     *
     * <p>No consulta la lista negra de revocación: eso lo comprueba el filtro por separado, y ambas
     * condiciones deben cumplirse.
     *
     * @param token JWT a validar
     * @param userDetails usuario cargado de la base con el que debe coincidir el claim {@code sub}
     * @return {@code true} si el token es del usuario y sigue vigente
     * @throws io.jsonwebtoken.JwtException si el token no es íntegro
     */
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
}
