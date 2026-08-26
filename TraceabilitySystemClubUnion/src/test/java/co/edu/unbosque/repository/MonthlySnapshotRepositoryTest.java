package co.edu.unbosque.repository;

import co.edu.unbosque.model.MonthlySnapshot;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import co.edu.unbosque.security.AesGcmEncryptionService;
import co.edu.unbosque.security.DeterministicEncryptionService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import({ AesGcmEncryptionService.class, DeterministicEncryptionService.class })
class MonthlySnapshotRepositoryTest {

	@Autowired private MonthlySnapshotRepository repo;

	private MonthlySnapshot snap(String ym, double billed) {
		MonthlySnapshot s = new MonthlySnapshot();
		s.setYearMonth(ym);
		s.setTotalBilled(billed);
		return s;
	}

	@Test
	void findByYearMonth_and_existsByYearMonth() {
		repo.save(snap("2026-07", 100.0));
		assertTrue(repo.existsByYearMonth("2026-07"));
		assertFalse(repo.existsByYearMonth("2026-06"));
		assertEquals(100.0, repo.findByYearMonth("2026-07").orElseThrow().getTotalBilled(), 0.001);
	}

	@Test
	void findAllByOrderByYearMonthAsc_ordersChronologically() {
		repo.save(snap("2026-08", 2.0));
		repo.save(snap("2026-06", 1.0));
		List<MonthlySnapshot> all = repo.findAllByOrderByYearMonthAsc();
		assertEquals("2026-06", all.get(0).getYearMonth());
		assertEquals("2026-08", all.get(1).getYearMonth());
	}
}
