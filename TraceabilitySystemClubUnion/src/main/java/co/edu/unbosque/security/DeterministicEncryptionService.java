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

@Component
public class DeterministicEncryptionService {

	public static final String PREFIX = "DET:v1:";

	private static final String TRANSFORMATION = "AES/GCM/NoPadding";
	private static final int IV_LENGTH_BYTES = 12;
	private static final int TAG_LENGTH_BITS = 128;

	private final SecretKeySpec aesKey;
	private final SecretKeySpec macKey;

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

	public boolean isEncrypted(String value) {
		return value != null && value.startsWith(PREFIX);
	}

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
