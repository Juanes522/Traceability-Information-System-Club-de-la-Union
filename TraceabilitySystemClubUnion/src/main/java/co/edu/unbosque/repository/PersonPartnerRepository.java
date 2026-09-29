package co.edu.unbosque.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import co.edu.unbosque.model.PersonPartner;

/**
 * Acceso a los socios y usuarios del sistema.
 *
 * <p>Su método más importante es {@link #findByIdentification(String)}, del que depende la
 * autenticación completa.
 *
 * <p>Nótese la <strong>ausencia</strong> de un {@code findByEmail}: el campo {@code email} de
 * {@link PersonPartner} se cifra con IV aleatorio y no es consultable, de modo que la búsqueda por
 * correo se resuelve recorriendo la tabla en
 * {@link co.edu.unbosque.service.PersonPartnerService#getByEmail(String)}.
 */
@Repository
public interface PersonPartnerRepository extends JpaRepository<PersonPartner, Long> {

	/**
	 * Busca un socio por su clave primaria.
	 *
	 * <p>Equivale a {@code findById} heredado de {@code JpaRepository}; se declara de forma explícita
	 * por legibilidad en los puntos de llamada.
	 *
	 * @param personId clave primaria
	 * @return el socio, si existe
	 */
	Optional<PersonPartner> findByPersonId(Long personId);

	/**
	 * Busca un socio por su identificación, que es el nombre de usuario del sistema.
	 *
	 * <p><strong>Consulta sobre una columna cifrada.</strong> Funciona porque el parámetro atraviesa
	 * {@link co.edu.unbosque.converter.DeterministicEncryptedStringConverter} antes de llegar al
	 * {@code WHERE}, de modo que la comparación es criptograma contra criptograma. Es posible solo
	 * porque ese campo usa cifrado determinista; con cifrado aleatorio no habría manera de resolverlo.
	 *
	 * <p>De este método depende
	 * {@link co.edu.unbosque.security.UserDetailsServiceImpl#loadUserByUsername(String)}, y por tanto
	 * toda la autenticación: se invoca en cada petición autenticada, dado que el JWT no lleva claim de
	 * rol.
	 *
	 * @param identification cédula o documento del socio, en claro
	 * @return el socio, si existe
	 */
	Optional<PersonPartner> findByIdentification(String identification);

	/**
	 * Busca socios por su primer nombre, con coincidencia exacta.
	 *
	 * <p>No es una búsqueda parcial: no hay {@code Containing} ni {@code Like}. La sensibilidad a
	 * mayúsculas depende de la <em>collation</em> de la base, no del código.
	 *
	 * @param firstName primer nombre exacto
	 * @return los socios coincidentes; lista vacía si no hay ninguno
	 */
	List<PersonPartner> findByFirstName(String firstName);

	/**
	 * Busca socios por su segundo nombre, con coincidencia exacta. Mismas consideraciones que
	 * {@link #findByFirstName(String)}.
	 *
	 * @param secondName segundo nombre exacto
	 * @return los socios coincidentes; lista vacía si no hay ninguno
	 */
	List<PersonPartner> findBySecondName(String secondName);

	/**
	 * Busca socios por número de acción.
	 *
	 * <p>Devuelve una lista y no un {@code Optional} porque <strong>{@code shareNumber} no tiene
	 * restricción de unicidad</strong>. Conviene saberlo al leer
	 * {@link co.edu.unbosque.controller.ReportController}, que al generar un estado de cuenta por número
	 * de acción toma el primer elemento de la lista, resolviendo la ambigüedad de forma arbitraria.
	 *
	 * @param shareNumber número de acción
	 * @return los socios asociados a esa acción; lista vacía si no hay ninguno
	 */
	List<PersonPartner> findByShareNumber(Long shareNumber);

}
