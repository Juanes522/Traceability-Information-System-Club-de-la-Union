package co.edu.unbosque.security;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Cifrado AES-256-GCM <strong>determinista</strong>, para el único campo personal que debe seguir siendo
 * consultable.
 *
 * <h2>Por qué existe este servicio</h2>
 *
 * <p>El nombre de usuario del sistema es la {@code identification} del socio, y esa columna está cifrada. Si
 * se cifrara con IV aleatorio como el resto de los datos de contacto, la consulta
 * {@link co.edu.unbosque.repository.PersonPartnerRepository#findByIdentification(String)} no podría
 * resolverse y <strong>el inicio de sesión sería imposible</strong>. Este servicio existe exclusivamente
 * para eso, y es la razón por la que el sistema tiene dos esquemas de cifrado en lugar de uno.
 *
 * <p>Su propiedad determinista sostiene también la restricción {@code unique = true} de esa columna, que la
 * base aplica sobre el criptograma.
 *
 * <h2>Cómo consigue el determinismo</h2>
 *
 * <p>En lugar de un IV aleatorio, deriva el IV del propio texto en claro, al estilo de las construcciones
 * SIV:
 *
 * <pre>
 * byte[] iv = Arrays.copyOf(hmac(textoEnClaro), 12);
 * </pre>
 *
 * <p>Misma entrada y misma clave producen siempre el mismo criptograma, que es exactamente lo que permite
 * comparar por igualdad en SQL.
 *
 * <h2>El precio, que es real y asumido</h2>
 *
 * <p><strong>El cifrado determinista revela igualdad.</strong> Frente a un espacio de búsqueda pequeño —una
 * cédula tiene diez dígitos—, quien pueda cifrar un valor candidato y comparar criptogramas obtiene un
 * oráculo de confirmación de pertenencia: puede averiguar si una persona concreta es socia del club sin
 * descifrar nada.
 *
 * <p>Se documenta como <strong>concesión de diseño y no como defecto</strong>: es el costo de poder
 * autenticar por identificación. Quien reconsidere esta decisión debe proponer a la vez otra forma de
 * resolver el inicio de sesión.
 *
 * <h2>Desviaciones respecto de la construcción que imita</h2>
 *
 * <ul>
 *   <li><strong>Reutilización de clave entre algoritmos.</strong> Los mismos 32 bytes sirven de clave AES y
 *       de clave HMAC-SHA256. La práctica estándar —y la propia construcción SIV— derivaría dos subclaves
 *       independientes con HKDF. No se aprecia explotación evidente aquí, pero es una desviación del diseño
 *       imitado.</li>
 *   <li><strong>El determinismo no se verifica al leer.</strong> {@link #decrypt(String)} no recomputa el IV
 *       a partir del texto descifrado, de modo que un valor cuyo IV no corresponda a
 *       {@code HMAC(claro)[0..12]} se descifra igualmente si la etiqueta GCM es correcta. La propiedad se
 *       impone solo en la escritura.</li>
 *   <li>A diferencia de {@link AesGcmEncryptionService#decrypt(String)}, no comprueba la longitud mínima del
 *       valor decodificado antes de trocearlo: un valor corrupto muy corto cae en el manejador genérico.</li>
 * </ul>
 *
 * @see co.edu.unbosque.converter.DeterministicEncryptedStringConverter
 */
@Component
public class DeterministicEncryptionService {

	/** Marca que identifica un valor cifrado por este servicio y habilita la migración progresiva. */
	public static final String PREFIX = "DET:v1:";

	private static final String TRANSFORMATION = "AES/GCM/NoPadding";
	private static final int IV_LENGTH_BYTES = 12;
	private static final int TAG_LENGTH_BITS = 128;

	private final SecretKeySpec aesKey;
	private final SecretKeySpec macKey;

	/**
	 * Valida la clave y deriva de ella las dos claves de trabajo.
	 *
	 * <p>Ambas se construyen sobre <strong>los mismos bytes</strong>, cambiando solo el algoritmo declarado.
	 * Véase la advertencia sobre reutilización de clave en la documentación de la clase.
	 *
	 * <p>La clave es la misma propiedad que consume {@link AesGcmEncryptionService}, de modo que un solo
	 * secreto gobierna los dos esquemas de cifrado del sistema.
	 *
	 * @param base64Key clave AES-256 en Base64, de {@code app.encryption.key}
	 * @throws IllegalStateException si el valor no es Base64 válido o no decodifica a exactamente 32 bytes
	 */
	public DeterministicEncryptionService(@Value("${app.encryption.key}") String base64Key) {
		byte[] keyBytes;
		try {
			keyBytes = Base64.getDecoder().decode(base64Key);
		} catch (IllegalArgumentException e) {
			throw new IllegalStateException("app.encryption.key no es Base64 válido", e);
		}
		if (keyBytes.length != 32) {
			throw new IllegalStateException("app.encryption.key debe ser 32 bytes (AES-256) en Base64");
		}
		this.aesKey = new SecretKeySpec(keyBytes, "AES");
		this.macKey = new SecretKeySpec(keyBytes, "HmacSHA256");
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
	 * Cifra un texto de forma determinista.
	 *
	 * <p>El IV se deriva del texto en claro mediante HMAC, de modo que <strong>la misma entrada produce
	 * siempre el mismo criptograma</strong>. Es lo que permite usar el resultado en un {@code WHERE} de
	 * igualdad y en una restricción de unicidad.
	 *
	 * <p>Nótese que cortocircuita tanto con {@code null} como con la cadena vacía, a diferencia de
	 * {@link AesGcmEncryptionService#encrypt(String)}, que solo lo hace con {@code null}.
	 *
	 * @param plaintext texto a cifrar; {@code null} y la cadena vacía se devuelven sin alterar
	 * @return el valor cifrado con prefijo {@value #PREFIX}
	 * @throws IllegalStateException si falla la operación criptográfica
	 */
	public String encrypt(String plaintext) {
		if (plaintext == null || plaintext.isEmpty()) {
			return plaintext;
		}
		try {
			byte[] pt = plaintext.getBytes(StandardCharsets.UTF_8);
			byte[] iv = Arrays.copyOf(hmac(pt), IV_LENGTH_BYTES);
			Cipher cipher = Cipher.getInstance(TRANSFORMATION);
			cipher.init(Cipher.ENCRYPT_MODE, aesKey, new GCMParameterSpec(TAG_LENGTH_BITS, iv));
			byte[] ct = cipher.doFinal(pt);
			byte[] combined = new byte[iv.length + ct.length];
			System.arraycopy(iv, 0, combined, 0, iv.length);
			System.arraycopy(ct, 0, combined, iv.length, ct.length);
			return PREFIX + Base64.getEncoder().encodeToString(combined);
		} catch (GeneralSecurityException e) {
			throw new IllegalStateException("Error cifrando (determinista)", e);
		}
	}

	/**
	 * Descifra un valor almacenado, o lo devuelve tal cual si no está cifrado.
	 *
	 * <p>Igual que en {@link AesGcmEncryptionService#decrypt(String)}, la comprobación de prefijo es el
	 * mecanismo de migración progresiva: una identificación almacenada en claro antes de introducir el
	 * cifrado se devuelve sin tocar y se cifra en el siguiente guardado de la entidad.
	 *
	 * <p><strong>No verifica que el IV corresponda al HMAC del texto descifrado</strong>, de modo que el
	 * determinismo se impone en la escritura pero no se comprueba en la lectura.
	 *
	 * @param stored contenido de la columna
	 * @return el texto en claro, o el valor original si no estaba cifrado
	 * @throws IllegalStateException si el dato está corrupto o la clave no corresponde
	 */
	public String decrypt(String stored) {
		if (stored == null || stored.isEmpty()) {
			return stored;
		}
		if (!isEncrypted(stored)) {
			return stored;
		}
		try {
			byte[] combined = Base64.getDecoder().decode(stored.substring(PREFIX.length()));
			byte[] iv = Arrays.copyOfRange(combined, 0, IV_LENGTH_BYTES);
			byte[] ct = Arrays.copyOfRange(combined, IV_LENGTH_BYTES, combined.length);
			Cipher cipher = Cipher.getInstance(TRANSFORMATION);
			cipher.init(Cipher.DECRYPT_MODE, aesKey, new GCMParameterSpec(TAG_LENGTH_BITS, iv));
			return new String(cipher.doFinal(ct), StandardCharsets.UTF_8);
		} catch (GeneralSecurityException | IllegalArgumentException e) {
			throw new IllegalStateException("Error descifrando (determinista)", e);
		}
	}

	/**
	 * Calcula el HMAC-SHA256 del que se toman los primeros doce bytes como IV.
	 *
	 * <p>Usar una función pseudoaleatoria con clave —y no un hash desnudo— es lo que impide que un atacante
	 * sin la clave pueda predecir el IV de un valor dado.
	 *
	 * @param data bytes del texto en claro
	 * @return el HMAC completo, de 32 bytes
	 * @throws IllegalStateException si el algoritmo no está disponible o la clave es inválida
	 */
	private byte[] hmac(byte[] data) {
		try {
			Mac mac = Mac.getInstance("HmacSHA256");
			mac.init(macKey);
			return mac.doFinal(data);
		} catch (GeneralSecurityException e) {
			throw new IllegalStateException("Error HMAC", e);
		}
	}
}
