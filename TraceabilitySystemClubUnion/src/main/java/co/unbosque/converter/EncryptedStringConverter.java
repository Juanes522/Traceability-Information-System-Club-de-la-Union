package co.unbosque.converter;

import co.unbosque.security.AesGcmEncryptionService;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class EncryptedStringConverter implements AttributeConverter<String, String> {

	private final AesGcmEncryptionService encryptionService;

	public EncryptedStringConverter(AesGcmEncryptionService encryptionService) {
		this.encryptionService = encryptionService;
	}

	@Override
	public String convertToDatabaseColumn(String attribute) {
		if (attribute == null || attribute.isEmpty()) {
			return attribute;
		}
		return encryptionService.encrypt(attribute);
	}

	@Override
	public String convertToEntityAttribute(String dbData) {
		if (dbData == null || dbData.isEmpty()) {
			return dbData;
		}
		return encryptionService.decrypt(dbData);
	}
}
