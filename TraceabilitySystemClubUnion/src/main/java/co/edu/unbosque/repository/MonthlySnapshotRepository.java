package co.edu.unbosque.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import co.edu.unbosque.model.MonthlySnapshot;

public interface MonthlySnapshotRepository extends JpaRepository<MonthlySnapshot, Long> {

	Optional<MonthlySnapshot> findByYearMonth(String yearMonth);

	boolean existsByYearMonth(String yearMonth);

	List<MonthlySnapshot> findAllByOrderByYearMonthAsc();
}
