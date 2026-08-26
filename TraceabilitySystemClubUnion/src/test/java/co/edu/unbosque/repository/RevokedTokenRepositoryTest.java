package co.edu.unbosque.repository;

import co.edu.unbosque.model.PersonPartner;
import co.edu.unbosque.model.RevokedToken;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class RevokedTokenRepositoryTest {

	@Autowired
	private RevokedTokenRepository repository;

	@Autowired
	private PersonPartnerRepository partnerRepo;

	@Test
	void existsByJti_trueOnlyForSavedJti() {
		repository.save(new RevokedToken("jti-guardado", LocalDateTime.now().plusHours(1)));
		assertTrue(repository.existsByJti("jti-guardado"));
		assertFalse(repository.existsByJti("jti-inexistente"));
	}

	@Test
	void deleteByExpiryDateBefore_removesOnlyExpired() {
		repository.save(new RevokedToken("jti-expirado", LocalDateTime.now().minusHours(1)));
		repository.save(new RevokedToken("jti-vigente", LocalDateTime.now().plusHours(1)));

		repository.deleteByExpiryDateBefore(LocalDateTime.now());

		assertFalse(repository.existsByJti("jti-expirado"));
		assertTrue(repository.existsByJti("jti-vigente"));
	}

	@Test
	void save_persistsPersonReference() {
		PersonPartner socio = new PersonPartner();
		socio.setIdentification("rt-owner-" + System.nanoTime());
		socio = partnerRepo.save(socio);
		RevokedToken saved = repository.save(new RevokedToken("jti-con-persona", LocalDateTime.now().plusHours(1), socio));
		RevokedToken reloaded = repository.findById(saved.getId()).orElseThrow();
		assertNotNull(reloaded.getPerson());
		assertEquals(socio.getPersonId(), reloaded.getPerson().getPersonId());
	}
}
