package co.edu.unbosque.converter;

import co.edu.unbosque.security.DeterministicEncryptionService;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Cifra y descifra de forma determinista el atributo {@code identification} de
 * {@link co.edu.unbosque.model.PersonPartner}, único campo que lo usa.
 *
 * <p>A diferencia de {@link EncryptedStringConverter}, que emplea un IV aleatorio, este conversor
 * delega en un servicio que deriva el IV del propio texto en claro, de modo que un mismo valor
 * produce siempre el mismo criptograma.
 *
 * <p>Esa propiedad es la que permite que
 * {@link co.edu.unbosque.repository.PersonPartnerRepository#findByIdentification(String)} funcione
 * como una comparación de igualdad sobre el criptograma, y por tanto <strong>la que hace posible el
 * inicio de sesión</strong> en
 * {@link co.edu.unbosque.security.UserDetailsServiceImpl#loadUserByUsername(String)}, cuyo nombre de
 * usuario es precisamente la identificación. También es lo que da sentido a la restricción
 * {@code unique = true} de esa columna, que la base aplica sobre el criptograma.
 *
 * <p>El precio asumido es explícito y conviene no perderlo de vista: <strong>el cifrado determinista
 * revela igualdad</strong>. Frente a un espacio de búsqueda pequeño —una cédula tiene diez dígitos—,
 * quien pueda cifrar un valor candidato y comparar criptogramas obtiene un oráculo de confirmación de
 * pertenencia. Es una concesión deliberada a cambio de poder autenticar por identificación, no un
 * descuido.
 *
 * <p>Los valores sin el prefijo {@code DET:v1:} se devuelven sin modificar, de modo que las filas
 * anteriores a la migración siguen siendo legibles y se cifran en el siguiente guardado de la
 * entidad.
 *
 * <p>Nótese que este conversor y {@link EncryptedStringConverter} tienen una estructura idéntica y se
 * diferencian únicamente en el servicio que inyectan.
 *
 * @see co.edu.unbosque.security.DeterministicEncryptionService
 */
@Converter
public class DeterministicEncryptedStringConverter implements AttributeConverter<String, String> {

	private final DeterministicEncryptionService encryptionService;

	/**
	 * @param encryptionService servicio de cifrado determinista. Recibirlo por constructor implica que
	 *                          <strong>Hibernate no puede instanciar este conversor por
	 *                          reflexión</strong>: depende de que Spring Boot haya conectado su
	 *                          {@code SpringBeanContainer} con Hibernate
	 */
	public DeterministicEncryptedStringConverter(DeterministicEncryptionService encryptionService) {
		this.encryptionService = encryptionService;
	}

	/**
	 * Cifra el valor antes de escribirlo en la columna.
	 *
	 * @param attribute valor en claro; {@code null} y la cadena vacía se devuelven sin alterar
	 * @return el criptograma con prefijo {@code DET:v1:}
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
	 * @param dbData contenido de la columna; si no lleva el prefijo {@code DET:v1:} se devuelve tal
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
