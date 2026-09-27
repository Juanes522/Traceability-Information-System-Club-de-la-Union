package co.edu.unbosque.security;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Cifrado autenticado AES-256-GCM con IV aleatorio, para los datos personales no consultables.
 *
 * <p>Lo usan {@link co.edu.unbosque.converter.EncryptedStringConverter} y
 * {@link co.edu.unbosque.converter.EncryptedStringArrayConverter}, es decir los campos {@code phone},
 * {@code cellPhone} y {@code email} de {@link co.edu.unbosque.model.PersonPartner}.
 *
 * <h2>IV aleatorio: la propiedad central y su precio</h2>
 *
 * <p>Cada operación genera un IV nuevo, de modo que <strong>el mismo texto en claro produce un criptograma
 * distinto cada vez</strong>. Es la práctica correcta para cifrado autenticado —impide inferir que dos
 * filas comparten valor—, y por eso es el servicio por defecto del sistema.
 *
 * <p>El precio es que sobre esas columnas <strong>no son posibles las búsquedas por igualdad, los índices
 * ni las restricciones de unicidad</strong>. Cuando esa capacidad es indispensable, como en el campo
 * {@code identification}, hay que recurrir a {@link DeterministicEncryptionService}.
 *
 * <h2>Compatibilidad con datos anteriores a la migración</h2>
 *
 * <p>{@link #decrypt(String)} devuelve sin alterar cualquier valor que no lleve el prefijo
 * {@value #PREFIX}. Esa única condición es todo el mecanismo de migración progresiva: las filas en claro
 * siguen siendo legibles y se cifran en el siguiente guardado de la entidad. Como la escritura siempre
 * cifra, el texto en claro no puede reaparecer.
 *
 * <h2>Limitaciones conocidas</h2>
 *
 * <ul>
 *   <li><strong>No hay rotación de clave.</strong> El {@code v1} del prefijo anticipa versionado, pero nada
 *       lee esa versión y solo existe una clave. Cambiar {@code app.encryption.key} vuelve indescifrable de
 *       forma permanente todo lo ya almacenado.</li>
 *   <li><strong>No se vincula AAD.</strong> La identidad de columna y de fila no está autenticada, de modo
 *       que un criptograma de {@code phone} podría trasladarse a {@code cell_phone} —o a otra fila— y
 *       descifraría correctamente.</li>
 *   <li>La clave es la misma que emplea {@link DeterministicEncryptionService}: una sola propiedad gobierna
 *       dos esquemas y tres conversores.</li>
 * </ul>
 *
 * <p>La expansión del criptograma es {@code 7 + ceil(4/3 · (28 + n))} caracteres, de donde salen los anchos
 * de columna declarados en la entidad y los del script de migración.
 */
@Component
public class AesGcmEncryptionService {

	/** Marca que identifica un valor cifrado por este servicio y habilita la migración progresiva. */
	public static final String PREFIX = "ENC:v1:";

	private static final String TRANSFORMATION = "AES/GCM/NoPadding";
	private static final int IV_LENGTH_BYTES = 12;
	private static final int TAG_LENGTH_BITS = 128;

	private final SecretKeySpec key;
	private final SecureRandom secureRandom = new SecureRandom();

	/**
	 * Valida la clave y prepara el servicio.
	 *
	 * <p>La validación es <strong>explícita y con mensajes claros</strong>, y falla al arrancar la aplicación
	 * en lugar de al primer uso. Es el comportamiento deseable para un parámetro criptográfico, y contrasta
	 * con {@link JwtUtil}, que delega la comprobación equivalente en una excepción de JJWT bastante menos
	 * informativa.
	 *
	 * @param base64Key clave AES-256 en Base64, de {@code app.encryption.key}
	 * @throws IllegalStateException si el valor no es Base64 válido o no decodifica a exactamente 32 bytes
	 */
	public AesGcmEncryptionService(@Value("${app.encryption.key}") String base64Key) {
		byte[] keyBytes;
		try {
			keyBytes = Base64.getDecoder().decode(base64Key);
		} catch (IllegalArgumentException e) {
			throw new IllegalStateException("app.encryption.key no es Base64 válido", e);
		}
		if (keyBytes.length != 32) {
			throw new IllegalStateException(
					"app.encryption.key debe ser una clave AES de 256 bits (32 bytes) en Base64; se recibieron "
							+ keyBytes.length + " bytes");
		}
		this.key = new SecretKeySpec(keyBytes, "AES");
	}

	/**
	 * Indica si un valor ya está cifrado por este servicio.
	 *
	 * @param value valor a inspeccionar
	 * @return {@code true} si comienza por {@value #PREFIX}
	 */
	public boolean isEncrypted(String value) {
		return value != null && value.startsWith(PREFIX);
	}

	/**
	 * Cifra un texto con un IV aleatorio nuevo.
	 *
	 * <p>El resultado es {@value #PREFIX} seguido del Base64 de la concatenación IV ‖ criptograma ‖ etiqueta
	 * de autenticación. Invocarlo dos veces con la misma entrada produce <strong>resultados
	 * distintos</strong>, por diseño.
	 *
	 * <p>Nótese que solo cortocircuita con {@code null}: la cadena vacía sí se cifra.
	 * {@link DeterministicEncryptionService#encrypt(String)} difiere en esto.
	 *
	 * @param plaintext texto a cifrar; {@code null} devuelve {@code null}
	 * @return el valor cifrado con prefijo
	 * @throws IllegalStateException si falla la operación criptográfica
	 */
	public String encrypt(String plaintext) {
		if (plaintext == null) {
			return null;
		}
		try {
			byte[] iv = new byte[IV_LENGTH_BYTES];
			secureRandom.nextBytes(iv);
			Cipher cipher = Cipher.getInstance(TRANSFORMATION);
			cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_LENGTH_BITS, iv));
			byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
			byte[] combined = new byte[iv.length + ciphertext.length];
			System.arraycopy(iv, 0, combined, 0, iv.length);
			System.arraycopy(ciphertext, 0, combined, iv.length, ciphertext.length);
			return PREFIX + Base64.getEncoder().encodeToString(combined);
		} catch (GeneralSecurityException e) {
			throw new IllegalStateException("Error cifrando valor con AES-256-GCM", e);
		}
	}

	/**
	 * Descifra un valor almacenado, o lo devuelve tal cual si no está cifrado.
	 *
	 * <p><strong>La comprobación de prefijo es el mecanismo completo de migración progresiva.</strong> Un
	 * valor sin {@value #PREFIX} es un dato anterior a la introducción del cifrado y se devuelve sin tocar,
	 * de modo que las bases preexistentes siguen siendo legibles sin ningún paso de conversión masiva.
	 *
	 * <p>La etiqueta GCM se verifica al descifrar, así que un valor manipulado o cifrado con otra clave no
	 * produce basura: lanza excepción.
	 *
	 * <p>Consideración operativa: esta excepción es una {@code IllegalStateException}, y
	 * {@link co.edu.unbosque.exception.GlobalExceptionHandler} <strong>no la maneja</strong>. Una clave
	 * incorrecta o rotada se manifiesta ante el cliente como HTTP 500 sin diagnóstico.
	 *
	 * @param storedValue contenido de la columna
	 * @return el texto en claro, o el valor original si no estaba cifrado
	 * @throws IllegalStateException si el dato está corrupto o la clave no corresponde
	 */
	public String decrypt(String storedValue) {
		if (storedValue == null) {
			return null;
		}
		if (!isEncrypted(storedValue)) {
			return storedValue;
		}
		try {
			byte[] combined = Base64.getDecoder().decode(storedValue.substring(PREFIX.length()));
			if (combined.length <= IV_LENGTH_BYTES) {
				throw new IllegalStateException(
						"Error descifrando valor AES-256-GCM (dato corrupto o clave incorrecta)");
			}
			byte[] iv = Arrays.copyOfRange(combined, 0, IV_LENGTH_BYTES);
			byte[] ciphertext = Arrays.copyOfRange(combined, IV_LENGTH_BYTES, combined.length);
			Cipher cipher = Cipher.getInstance(TRANSFORMATION);
			cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_LENGTH_BITS, iv));
			return new String(cipher.doFinal(ciphertext), StandardCharsets.UTF_8);
		} catch (GeneralSecurityException | IllegalArgumentException e) {
			throw new IllegalStateException(
					"Error descifrando valor AES-256-GCM (dato corrupto o clave incorrecta)", e);
		}
	}
}
