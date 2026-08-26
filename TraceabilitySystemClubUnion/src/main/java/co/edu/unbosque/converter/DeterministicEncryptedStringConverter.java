package co.edu.unbosque.converter;

import co.edu.unbosque.security.DeterministicEncryptionService;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class DeterministicEncryptedStringConverter implements AttributeConverter<String, String> {

	private final DeterministicEncryptionService encryptionService;

	public DeterministicEncryptedStringConverter(DeterministicEncryptionService encryptionService) {
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
