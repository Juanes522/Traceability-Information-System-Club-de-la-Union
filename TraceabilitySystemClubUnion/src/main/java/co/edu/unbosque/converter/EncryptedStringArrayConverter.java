package co.edu.unbosque.converter;

import co.edu.unbosque.security.AesGcmEncryptionService;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Persiste el arreglo de correos de {@link co.edu.unbosque.model.PersonPartner} como un único valor
 * cifrado.
 *
 * <p>La estrategia es <strong>unir con comas y cifrar la cadena resultante como un solo bloque</strong>,
 * no cifrar elemento por elemento. De ahí varias consecuencias que conviene tener presentes:
 *
 * <ul>
 *   <li><strong>No se puede consultar por correo.</strong> El criptograma corresponde al CSV completo y
 *       usa IV aleatorio, así que no existe {@code findByEmail}. Buscar un socio por su dirección obliga
 *       a recorrer la tabla descifrando fila por fila, que es lo que hace
 *       {@link co.edu.unbosque.service.PersonPartnerService#getByEmail(String)}.</li>
 *   <li><strong>La coma es separador y no hay escape.</strong> Es admisible porque una dirección de
 *       correo no contiene comas, pero sería un defecto latente si el conversor se reutilizara para otro
 *       tipo de dato.</li>
 *   <li><strong>El recorrido no es simétrico.</strong> {@code split} descarta las cadenas vacías
 *       finales, de modo que {@code "a@x.com,"} regresa como un arreglo de un solo elemento.</li>
 *   <li><strong>El tamaño no se valida.</strong> Los 1000 caracteres de la columna admiten unos 716
 *       bytes de texto unido, del orden de 15 a 20 direcciones. Superarlo produce un error de
 *       truncamiento de SQL Server que ningún manejador captura, y que llega al cliente como HTTP 500.</li>
 * </ul>
 *
 * <p>La migración desde el predecesor {@link StringArrayConverter} funciona sin intervención: un CSV en
 * claro carece del prefijo {@code ENC:v1:}, se devuelve sin descifrar y se divide igual.
 * {@code EncryptedStringArrayConverterTest} fija ese comportamiento explícitamente.
 *
 * @see co.edu.unbosque.security.AesGcmEncryptionService
 */
@Converter
public class EncryptedStringArrayConverter implements AttributeConverter<String[], String> {

	/** Separador entre direcciones. No se escapa en ningún caso. */
	private static final String SEPARATOR = ",";

	private final AesGcmEncryptionService encryptionService;

	/**
	 * Crea el conversor con el servicio de cifrado.
	 *
	 * @param encryptionService servicio de cifrado AES-256-GCM, inyectado por Spring
	 */
	public EncryptedStringArrayConverter(AesGcmEncryptionService encryptionService) {
		this.encryptionService = encryptionService;
	}

	/**
	 * Une el arreglo con comas y cifra el resultado completo.
	 *
	 * @param attribute direcciones a persistir
	 * @return el criptograma con prefijo {@code ENC:v1:}, o {@code null} si el arreglo es nulo o vacío.
	 *         Nótese que un arreglo vacío se normaliza a {@code NULL} en la base, a diferencia de los
	 *         conversores de cadena simple, que conservan la cadena vacía
	 */
	@Override
	public String convertToDatabaseColumn(String[] attribute) {
		if (attribute == null || attribute.length == 0) {
			return null;
		}
		return encryptionService.encrypt(String.join(SEPARATOR, attribute));
	}

	/**
	 * Descifra el valor almacenado y lo divide en direcciones.
	 *
	 * @param dbData contenido de la columna; si no lleva el prefijo {@code ENC:v1:} se trata como CSV en
	 *               claro heredado
	 * @return las direcciones; <strong>nunca {@code null}</strong>, sino un arreglo vacío cuando no hay
	 *         datos
	 */
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
