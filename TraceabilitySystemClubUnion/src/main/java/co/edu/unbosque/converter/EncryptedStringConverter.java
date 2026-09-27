package co.edu.unbosque.converter;

import co.edu.unbosque.security.AesGcmEncryptionService;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Cifra y descifra los teléfonos de {@link co.edu.unbosque.model.PersonPartner} con AES-256-GCM e IV
 * aleatorio.
 *
 * <p>Se aplica a {@code phone} y {@code cellPhone}. Al usar un IV distinto en cada operación, el mismo
 * número produce criptogramas distintos, lo cual es la práctica correcta para cifrado autenticado pero
 * <strong>vuelve esas columnas no consultables</strong>: no admiten búsqueda por igualdad, ni índice,
 * ni restricción de unicidad. No hay ninguna consulta en el sistema que filtre por teléfono, de modo
 * que la limitación no estorba.
 *
 * <p>Compárese con {@link DeterministicEncryptedStringConverter}, que sacrifica esa propiedad de
 * seguridad justamente porque su campo sí debe ser consultable.
 *
 * <p>Los valores sin el prefijo {@code ENC:v1:} se devuelven sin modificar, lo que mantiene legibles
 * las filas anteriores a la migración hasta que se vuelvan a guardar.
 *
 * <p>El criptograma ocupa aproximadamente {@code 7 + 4/3 · (28 + n)} caracteres, de donde sale el
 * ancho de 512 declarado en esas columnas.
 *
 * @see co.edu.unbosque.security.AesGcmEncryptionService
 */
@Converter
public class EncryptedStringConverter implements AttributeConverter<String, String> {

	private final AesGcmEncryptionService encryptionService;

	/**
	 * @param encryptionService servicio de cifrado AES-256-GCM. Al recibirse por constructor, este
	 *                          conversor solo puede crearlo Spring, no Hibernate por reflexión
	 */
	public EncryptedStringConverter(AesGcmEncryptionService encryptionService) {
		this.encryptionService = encryptionService;
	}

	/**
	 * Cifra el valor antes de escribirlo en la columna.
	 *
	 * @param attribute valor en claro; {@code null} y la cadena vacía se devuelven sin alterar
	 * @return el criptograma con prefijo {@code ENC:v1:}, distinto en cada llamada aunque la entrada
	 *         sea la misma
	 */
	@Override
	public String convertToDatabaseColumn(String attribute) {
		if (attribute == null || attribute.isEmpty()) {
			return attribute;
		}
		return encryptionService.encrypt(attribute);
	}

	/**
	 * Descifra el valor almacenado al leerlo de la base.
	 *
	 * @param dbData contenido de la columna; si no lleva el prefijo {@code ENC:v1:} se devuelve tal
	 *               cual, que es el mecanismo de compatibilidad con filas anteriores a la migración
	 * @return el valor en claro
	 */
	@Override
	public String convertToEntityAttribute(String dbData) {
		if (dbData == null || dbData.isEmpty()) {
			return dbData;
		}
		return encryptionService.decrypt(dbData);
	}
}
