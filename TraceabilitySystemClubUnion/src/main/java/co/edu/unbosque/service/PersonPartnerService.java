package co.edu.unbosque.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.model.PartnerConsumption;
import co.edu.unbosque.model.PersonPartner;
import co.edu.unbosque.repository.PersonPartnerRepository;

/**
 * Operaciones sobre socios y usuarios del sistema.
 *
 * <p>Es una capa delgada sobre {@link PersonPartnerRepository}: no contiene reglas de negocio, no valida y
 * no comprueba autorización. Su valor está en dos aspectos que sí aporta: define el <strong>límite
 * transaccional</strong> —{@code @Transactional} a nivel de clase— y resuelve la búsqueda por correo, que el
 * repositorio no puede expresar.
 *
 * <p>Ese límite transaccional es más relevante de lo que parece: {@link PartnerSyncService} no es
 * transaccional, de modo que en una sincronización <strong>cada llamada a {@link #savePartner} es su propia
 * transacción</strong>, y un fallo a mitad del proceso deja la base parcialmente actualizada.
 *
 * <p><strong>Convención del paquete que conviene conocer:</strong> «no encontrado» y «sin resultados» se
 * expresan devolviendo {@code null}, no {@code Optional} ni una colección vacía. Eso obliga a comprobar
 * nulidad en cada controlador y es la razón por la que la elección entre respuestas 204 y 404 resulta
 * irregular en la API.
 */
@Service
@Transactional
public class PersonPartnerService {

	@Autowired
	private PersonPartnerRepository partnerRepo;

	/**
	 * Constructor sin argumentos requerido para la deserialización del cuerpo de la petición.
	 */
	public PersonPartnerService() {
	}

	/**
	 * Crea o actualiza un socio.
	 *
	 * <p>Delega en {@code save}, de modo que inserta o actualiza según tenga clave primaria. <strong>No
	 * valida nada</strong> ni comprueba previamente la unicidad de la identificación: una colisión se
	 * manifiesta como violación de restricción de la base, que ningún manejador traduce y llega al cliente
	 * como HTTP 500.
	 *
	 * <p>Al guardar la entidad completa, cifra de paso cualquier dato de contacto que siguiera en claro.
	 *
	 * @param save socio a persistir
	 */
	public void savePartner(PersonPartner save) {
		partnerRepo.save(save);
	}

	/**
	 * Devuelve todos los socios, sin paginar.
	 *
	 * <p>Descifra los datos de contacto de <strong>cada fila</strong>, de modo que el costo crece con el
	 * tamaño del padrón. Lo consume {@code GET /personpartner/getall}, que el frontend del gestor usa para
	 * cargar el padrón completo en memoria y paginarlo en el cliente.
	 *
	 * @return todos los socios; lista vacía si no hay ninguno
	 */
	public List<PersonPartner> getAll() {
		return partnerRepo.findAll();
	}

	/**
	 * Devuelve una página de socios.
	 *
	 * <p>Es la alternativa preferible a {@link #getAll()}, y la que usa la pantalla de administración.
	 *
	 * @param pageable página solicitada
	 * @return la página de socios
	 */
	public Page<PersonPartner> getAllPaged(Pageable pageable) {
		return partnerRepo.findAll(pageable);
	}

	/**
	 * Busca un socio por su clave primaria. Sin uso actualmente.
	 *
	 * @param id clave primaria
	 * @return el socio, o {@code null} si no existe
	 */
	public PersonPartner getById(Long id) {
		Optional<PersonPartner> found = partnerRepo.findByPersonId(id);
		if (found.isPresent()) {
			return found.get();
		}
		return null;
	}

	/**
	 * Busca un socio por su identificación.
	 *
	 * <p>Es el método más usado de la clase: lo invocan el inicio de sesión, el cambio de contraseña, la
	 * aceptación del consentimiento, el cierre de sesión, la sincronización y los endpoints de datos propios.
	 *
	 * <p>La consulta funciona sobre la columna cifrada porque el parámetro atraviesa el conversor determinista
	 * antes del {@code WHERE}.
	 *
	 * @param identification cédula del socio, en claro
	 * @return el socio, o {@code null} si no existe
	 */
	public PersonPartner getByIdentification(String identification) {
		Optional<PersonPartner> found = partnerRepo.findByIdentification(identification);
		if (found.isPresent()) {
			return found.get();
		}
		return null;
	}

	/**
	 * Busca un socio por cualquiera de sus direcciones de correo, ignorando mayúsculas.
	 *
	 * <p><strong>Recorre la tabla completa descifrando cada fila.</strong> No es un descuido de
	 * implementación: es la única forma posible. El campo {@code email} se cifra con IV aleatorio, de modo que
	 * su criptograma no es comparable ni indexable, y el conversor cifra además el arreglo entero como un solo
	 * bloque. No existe ni puede existir un {@code findByEmail} en el repositorio.
	 *
	 * <p>Consideraciones de costo, relevantes porque el único invocador es un endpoint <strong>anónimo</strong>
	 * ({@code POST /auth/forgot-password}): el costo es lineal en el número de socios, con un descifrado
	 * AES-GCM por fila. Lo contiene la limitación de tasa de tres solicitudes por hora y por IP —que, con la
	 * lista de proxies de confianza vacía, es un límite global y no por cliente.
	 *
	 * <p>Devuelve el primer socio coincidente. Nada garantiza que una dirección de correo sea única entre
	 * socios, de modo que si estuviera repetida la resolución sería arbitraria.
	 *
	 * @param email dirección a buscar; se recorta y se compara sin distinguir mayúsculas
	 * @return el socio, o {@code null} si el correo es nulo, está en blanco o no pertenece a nadie
	 */
	public PersonPartner getByEmail(String email) {
		if (email == null || email.isBlank()) {
			return null;
		}
		String target = email.trim();
		for (PersonPartner partner : partnerRepo.findAll()) {
			String[] emails = partner.getEmail();
			if (emails == null) {
				continue;
			}
			for (String candidate : emails) {
				if (candidate != null && target.equalsIgnoreCase(candidate.trim())) {
					return partner;
				}
			}
		}
		return null;
	}

	/**
	 * Busca socios por su primer nombre, con coincidencia exacta.
	 *
	 * @param firstName primer nombre exacto
	 * @return los socios coincidentes, o <strong>{@code null}</strong> si no hay ninguno; nunca una lista
	 *         vacía. El controlador traduce ese nulo a una respuesta 204
	 */
	public List<PersonPartner> getByFirstName(String firstName) {
		List<PersonPartner> list = partnerRepo.findByFirstName(firstName);
		if (!list.isEmpty()) {
			return list;
		}
		return null;
	}

	/**
	 * Busca socios por su segundo nombre, con coincidencia exacta.
	 *
	 * @param secondName segundo nombre exacto
	 * @return los socios coincidentes, o {@code null} si no hay ninguno
	 */
	public List<PersonPartner> getBySecondName(String secondName) {
		List<PersonPartner> list = partnerRepo.findBySecondName(secondName);
		if (!list.isEmpty()) {
			return list;
		}
		return null;
	}

	/**
	 * Busca los socios asociados a un número de acción.
	 *
	 * <p>Devuelve una lista porque el número de acción <strong>no es único</strong>. Conviene tenerlo presente
	 * al leer {@link co.edu.unbosque.controller.ReportController}, que para generar un estado de cuenta por
	 * número de acción toma el primer elemento, resolviendo la ambigüedad de forma arbitraria.
	 *
	 * @param shareNumber número de acción
	 * @return los socios asociados, o {@code null} si no hay ninguno
	 */
	public List<PersonPartner> getByShareNumber(Long shareNumber) {
		List<PersonPartner> list = partnerRepo.findByShareNumber(shareNumber);
		if (!list.isEmpty()) {
			return list;
		}
		return null;
	}

	/**
	 * Devuelve los consumos de un socio recorriendo la colección de la entidad.
	 *
	 * <p>Sin uso actualmente, y con una errata en el nombre —{@code Comsuption} por {@code Consumption}— que se
	 * conserva porque corregirla sería un cambio de API.
	 *
	 * <p>El acceso a los consumos en producción no pasa por aquí, sino por
	 * {@link PartnerConsumptionService} con paginación: recorrer la colección de la entidad cargaría el
	 * historial completo del socio en memoria.
	 *
	 * @param id clave primaria del socio
	 * @return sus consumos, o {@code null} si el socio no existe
	 */
	public List<PartnerConsumption> getByComsuption(Long id) {
		Optional<PersonPartner> found = partnerRepo.findByPersonId(id);
		if (found.isPresent()) {
			return found.get().getConsumptions();
		}
		return null;
	}

}
