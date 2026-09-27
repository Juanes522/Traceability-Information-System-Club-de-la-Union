/**
 * Autenticación, autorización y criptografía.
 *
 * <h2>Cadena de filtros</h2>
 *
 * <p>{@link co.edu.unbosque.security.SecurityConfig} registra dos filtros propios, y el orden
 * resultante es:
 *
 * <pre>
 * RateLimitFilter → JwtAuthenticationFilter → UsernamePasswordAuthenticationFilter
 * </pre>
 *
 * <p>La limitación de tasa ocurre deliberadamente <strong>antes</strong> de cualquier
 * autenticación, para proteger el inicio de sesión de una avalancha anónima.
 *
 * <h2>Autenticación</h2>
 *
 * <p>JWT firmado con HS256 y vigencia de 8 horas. El conjunto de claims es exactamente
 * {@code jti}, {@code sub}, {@code iat} y {@code exp}: <strong>no hay claim de rol</strong>, de
 * modo que {@link co.edu.unbosque.security.UserDetailsServiceImpl} resuelve las autoridades
 * contra la base de datos en cada petición. Eso hace que revocar un privilegio surta efecto de
 * inmediato, a cambio de dos consultas por petición autenticada (la del usuario y la de la lista
 * negra de tokens).
 *
 * <p>El nombre de usuario es la {@code identification} del socio, que está cifrada en la base.
 * Ese hecho es la razón de ser de
 * {@link co.edu.unbosque.security.DeterministicEncryptionService}.
 *
 * <h2>Autorización</h2>
 *
 * <p>En dos planos: la cadena declara qué rutas son públicas, y {@code @PreAuthorize} sobre los
 * métodos de controlador decide qué rol puede invocar cada operación —habilitado por
 * {@code @EnableMethodSecurity}. Los tres roles son {@code ROLE_PARTNER}, {@code ROLE_MANAGER} y
 * {@code ROLE_ADMIN}, y cada usuario tiene <strong>exactamente uno</strong>.
 *
 * <h2>Criptografía</h2>
 *
 * <p>Dos servicios, ambos AES-256-GCM y ambos derivando su clave de la misma propiedad
 * {@code app.encryption.key}:
 *
 * <ul>
 *   <li>{@link co.edu.unbosque.security.AesGcmEncryptionService} — IV aleatorio, no consultable.</li>
 *   <li>{@link co.edu.unbosque.security.DeterministicEncryptionService} — IV derivado del texto en
 *       claro, consultable por igualdad a costa de revelar igualdad.</li>
 * </ul>
 *
 * <h2>Advertencia: dos estrategias opuestas de resolución de IP</h2>
 *
 * <p>Este paquete contiene dos implementaciones contradictorias.
 * {@link co.edu.unbosque.security.RateLimitFilter} solo honra {@code X-Forwarded-For} si el par
 * figura en una lista blanca de proxies de confianza —lo correcto—, mientras que
 * {@link co.edu.unbosque.security.HttpRequestUtils}, que alimenta toda la auditoría, confía en esa
 * cabecera sin condición. La misma petición se limita por su dirección real y se audita bajo una
 * dirección falsificable.
 */
package co.edu.unbosque.security;
