package co.edu.unbosque.repository;

import co.edu.unbosque.converter.DeterministicEncryptedStringConverter;
import co.edu.unbosque.converter.EncryptedStringArrayConverter;
import co.edu.unbosque.converter.EncryptedStringConverter;
import co.edu.unbosque.model.PersonPartner;
import co.edu.unbosque.security.AesGcmEncryptionService;
import co.edu.unbosque.security.DeterministicEncryptionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@Import({ DeterministicEncryptionService.class, DeterministicEncryptedStringConverter.class,
		AesGcmEncryptionService.class, EncryptedStringConverter.class, EncryptedStringArrayConverter.class })
/**
 * Verifica que la identificación se almacena cifrada y sigue siendo consultable.
 *
 * <p>Fija las dos mitades del compromiso de diseño: que la columna <strong>no contiene el valor en claro</strong>, y que aun
 * así la búsqueda por identificación lo encuentra, porque el parámetro atraviesa el conversor determinista antes de la
 * comparación.
 *
 * <p>Es también la prueba que documenta la dependencia invisible de los conversores respecto al contenedor: debe importar los
 * servicios de cifrado explícitamente, porque en una prueba de solo persistencia Hibernate no puede instanciarlos por sí
 * solo.
 */
class PersonPartnerIdentificationEncryptionTest {

	@Autowired
	private TestEntityManager em;

	@Autowired
	private PersonPartnerRepository repository;

	@Test
	void findByIdentification_matchesEncryptedStoredValue() {
		PersonPartner p = new PersonPartner();
		p.setIdentification("0102030405");
		p.setRole("PARTNER");
		p.setPartnerState(true);
		p.setShareNumber(100L);
		em.persist(p);
		em.flush();
		em.clear();

		Optional<PersonPartner> found = repository.findByIdentification("0102030405");

		assertTrue(found.isPresent());
		assertEquals("0102030405", found.get().getIdentification());
	}

	@Test
	void storedColumn_isEncryptedAtRest() {
		PersonPartner p = new PersonPartner();
		p.setIdentification("0102030405");
		p.setRole("PARTNER");
		p.setPartnerState(true);
		p.setShareNumber(100L);
		em.persist(p);
		em.flush();

		Object raw = em.getEntityManager()
				.createNativeQuery("SELECT identification FROM person_partner")
				.getSingleResult();

		assertTrue(raw.toString().startsWith("DET:v1:"));
	}
}
