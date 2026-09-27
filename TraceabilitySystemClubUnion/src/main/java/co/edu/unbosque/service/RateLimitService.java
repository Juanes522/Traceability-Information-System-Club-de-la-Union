package co.edu.unbosque.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
/**
 * Administra las cubetas de limitación de tasa que protegen la autenticación.
 *
 * <p>Implementa tres cubetas independientes con Bucket4j, almacenadas en cachés de Caffeine con expiración por
 * acceso de una hora y un máximo de 100 000 entradas:
 *
 * <table border="1">
 *   <caption>Cubetas y su propósito</caption>
 *   <tr><th>Cubeta</th><th>Clave</th><th>Valor por defecto</th><th>Protege de</th></tr>
 *   <tr><td>Inicio de sesión por IP</td><td>dirección IP</td><td>10 / 60 s</td>
 *       <td>Fuerza bruta distribuida sobre muchas cuentas</td></tr>
 *   <tr><td>Fallos por usuario</td><td>nombre de usuario</td><td>5 / 60 s</td>
 *       <td>Fuerza bruta concentrada en una cuenta</td></tr>
 *   <tr><td>Recuperación por IP</td><td>dirección IP</td><td>3 / 3600 s</td>
 *       <td>Abuso de un endpoint anónimo costoso</td></tr>
 * </table>
 *
 * <p>La cubeta de recuperación es mucho más estricta por dos razones: cada solicitud envía un correo real y, además,
 * provoca un recorrido completo de la tabla de socios con descifrado por fila.
 *
 * <p>Las dos cubetas por IP las consume {@link co.edu.unbosque.security.RateLimitFilter}; la de fallos por usuario
 * la gestiona {@link co.edu.unbosque.controller.AuthController} a mano, en torno al intento de autenticación.
 *
 * <p><strong>El estado es por instancia de JVM.</strong> Caffeine es una caché en memoria, no un almacén
 * compartido: escalar horizontalmente multiplica cada límite por el número de instancias, y un reinicio lo borra.
 * Trasladarlo a un almacén compartido, como Redis, sería el cambio necesario para un despliegue con varias
 * instancias.
 *
 * @see co.edu.unbosque.security.RateLimitFilter
 */
public class RateLimitService {

	private final long loginIpCapacity;
	private final Duration loginIpWindow;
	private final long loginUserCapacity;
	private final Duration loginUserWindow;
	private final long forgotPasswordCapacity;
	private final Duration forgotPasswordWindow;

	private final Cache<String, Bucket> loginIpBuckets;
	private final Cache<String, Bucket> userFailureBuckets;
	private final Cache<String, Bucket> forgotPasswordBuckets;

	public RateLimitService(
			@Value("${ratelimit.login.ip.capacity:10}") long loginIpCapacity,
			@Value("${ratelimit.login.ip.window-seconds:60}") long loginIpWindowSeconds,
			@Value("${ratelimit.login.user.capacity:5}") long loginUserCapacity,
			@Value("${ratelimit.login.user.window-seconds:60}") long loginUserWindowSeconds,
			@Value("${ratelimit.forgot-password.ip.capacity:3}") long forgotPasswordCapacity,
			@Value("${ratelimit.forgot-password.ip.window-seconds:3600}") long forgotPasswordWindowSeconds) {
		this.loginIpCapacity = loginIpCapacity;
		this.loginIpWindow = Duration.ofSeconds(loginIpWindowSeconds);
		this.loginUserCapacity = loginUserCapacity;
		this.loginUserWindow = Duration.ofSeconds(loginUserWindowSeconds);
		this.forgotPasswordCapacity = forgotPasswordCapacity;
		this.forgotPasswordWindow = Duration.ofSeconds(forgotPasswordWindowSeconds);
		this.loginIpBuckets = newCache();
		this.userFailureBuckets = newCache();
		this.forgotPasswordBuckets = newCache();
	}

	/**
	 * Intenta consumir un intento de inicio de sesión para una IP.
	 *
	 * <p>Devuelve la sonda completa, no un booleano, porque el llamante necesita además el tiempo restante para la
	 * recarga y así poder emitir la cabecera {@code Retry-After}.
	 *
	 * @param ip dirección del cliente
	 * @return la sonda, con el resultado del consumo y el tiempo hasta la recarga
	 */
	public ConsumptionProbe tryConsumeLoginByIp(String ip) {
		Bucket bucket = loginIpBuckets.get(ip, k -> newBucket(loginIpCapacity, loginIpWindow));
		return bucket.tryConsumeAndReturnRemaining(1);
	}

	/**
	 * Intenta consumir una solicitud de recuperación de contraseña para una IP.
	 *
	 * <p>Cubeta deliberadamente más estricta que la de inicio de sesión: tres por hora frente a diez por minuto.
	 *
	 * @param ip dirección del cliente
	 * @return la sonda, con el resultado del consumo y el tiempo hasta la recarga
	 */
	public ConsumptionProbe tryConsumeForgotPassword(String ip) {
		Bucket bucket = forgotPasswordBuckets.get(ip, k -> newBucket(forgotPasswordCapacity, forgotPasswordWindow));
		return bucket.tryConsumeAndReturnRemaining(1);
	}

	/**
	 * Indica si una cuenta acumuló demasiados fallos de autenticación recientes.
	 *
	 * <p><strong>Consulta sin consumir:</strong> se limita a mirar los tokens disponibles, de modo que comprobar el
	 * bloqueo no lo agrava. El consumo lo hace {@link #registerFailedLogin(String)}, y solo ante un fallo real.
	 *
	 * <p>Una cuenta sin cubeta —que nunca ha fallado— se considera no bloqueada.
	 *
	 * @param username nombre de usuario a comprobar
	 * @return {@code true} si la cuenta agotó su cupo de fallos
	 */
	public boolean isUserBlocked(String username) {
		if (username == null || username.isBlank()) {
			return false;
		}
		Bucket bucket = userFailureBuckets.getIfPresent(username);
		return bucket != null && bucket.getAvailableTokens() <= 0;
	}

	/**
	 * Consume un intento de la cubeta de la cuenta, tras un fallo de autenticación.
	 *
	 * @param username nombre de usuario que falló al autenticarse
	 */
	public void registerFailedLogin(String username) {
		if (username == null || username.isBlank()) {
			return;
		}
		Bucket bucket = userFailureBuckets.get(username, k -> newBucket(loginUserCapacity, loginUserWindow));
		bucket.tryConsume(1);
	}

	/**
	 * Descarta la cubeta de fallos de una cuenta, tras un inicio de sesión exitoso.
	 *
	 * <p>Que el acierto borre el historial de fallos evita que un usuario legítimo que se equivocó varias veces
	 * quede penalizado una vez ya demostró conocer su contraseña.
	 *
	 * @param username nombre de usuario que se autenticó correctamente
	 */
	public void resetUserFailures(String username) {
		if (username == null || username.isBlank()) {
			return;
		}
		userFailureBuckets.invalidate(username);
	}

	private Cache<String, Bucket> newCache() {
		return Caffeine.newBuilder()
				.expireAfterAccess(Duration.ofHours(1))
				.maximumSize(100_000)
				.build();
	}

	private Bucket newBucket(long capacity, Duration window) {
		return Bucket.builder()
				.addLimit(Bandwidth.builder().capacity(capacity).refillGreedy(capacity, window).build())
				.build();
	}
}
