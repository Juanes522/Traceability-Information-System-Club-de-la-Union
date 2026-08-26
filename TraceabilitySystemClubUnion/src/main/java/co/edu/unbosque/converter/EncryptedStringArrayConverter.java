package co.edu.unbosque.converter;

import co.edu.unbosque.security.AesGcmEncryptionService;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class EncryptedStringArrayConverter implements AttributeConverter<String[], String> {

	private static final String SEPARATOR = ",";

	private final AesGcmEncryptionService encryptionService;

	public EncryptedStringArrayConverter(AesGcmEncryptionService encryptionService) {
		this.encryptionService = encryptionService;
	}

	@Override
	public String convertToDatabaseColumn(String[] attribute) {
		if (attribute == null || attribute.length == 0) {
			return null;
		}
		return encryptionService.encrypt(String.join(SEPARATOR, attribute));
	}

	@Override
	public String[] convertToEntityAttribute(String dbData) {
		if (dbData == null || dbData.isEmpty()) {
			return new String[0];
		}
		String plaintext = encryptionService.decrypt(dbData);
		if (plaintext == null || plaintext.isEmpty()) {
			return new String[0];
		}
		return plaintext.split(SEPARATOR);
	}
}
