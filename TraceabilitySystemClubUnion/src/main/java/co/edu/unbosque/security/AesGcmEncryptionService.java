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

@Component
public class AesGcmEncryptionService {

	public static final String PREFIX = "ENC:v1:";

	private static final String TRANSFORMATION = "AES/GCM/NoPadding";
	private static final int IV_LENGTH_BYTES = 12;
	private static final int TAG_LENGTH_BITS = 128;

	private final SecretKeySpec key;
	private final SecureRandom secureRandom = new SecureRandom();

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

	public boolean isEncrypted(String value) {
		return value != null && value.startsWith(PREFIX);
	}

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
