package co.edu.unbosque.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import co.edu.unbosque.dto.ConsumptionRowView;
import co.edu.unbosque.model.PartnerConsumption;

public interface PartnerConsumptionRepository extends JpaRepository<PartnerConsumption, Long> {

	public Optional<PartnerConsumption> findByConsumptionId(Long consumptionId);

	public List<PartnerConsumption> findByPartnerPersonId(Long personId);

	Page<PartnerConsumption> findByPartnerPersonId(Long personId, Pageable pageable);

	Page<PartnerConsumption> findByPartnerPersonIdAndConsumptionOpeningBetween(
			Long personId, LocalDateTime from, LocalDateTime to, Pageable pageable);

	public List<PartnerConsumption> findByEnviroment(String enviroment);

	Page<PartnerConsumption> findByEnviroment(String enviroment, Pageable pageable);

	Page<PartnerConsumption> findByEnviromentAndConsumptionOpeningBetween(
			String enviroment, LocalDateTime from, LocalDateTime to, Pageable pageable);

	List<PartnerConsumption> findByConsumptionOpeningBetween(LocalDateTime from, LocalDateTime to);

	List<PartnerConsumption> findByEnviromentAndConsumptionOpeningBetween(
			String enviroment, LocalDateTime from, LocalDateTime to);

	List<PartnerConsumption> findByPartnerPersonIdAndConsumptionOpeningBetween(
			Long personId, LocalDateTime from, LocalDateTime to);

	@Query("SELECT COALESCE(SUM(c.consumptionValue),0), COALESCE(SUM(c.iva),0), " +
			"COALESCE(SUM(c.service),0), COALESCE(SUM(c.tip),0), COUNT(c) " +
			"FROM PartnerConsumption c WHERE c.consumptionOpening BETWEEN :from AND :to")
	List<Object[]> aggregateSummary(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

	@Query("SELECT c.enviroment, SUM(COALESCE(c.consumptionValue,0) + COALESCE(c.iva,0) + COALESCE(c.service,0) + COALESCE(c.tip,0)), COUNT(c) " +
			"FROM PartnerConsumption c WHERE c.consumptionOpening BETWEEN :from AND :to " +
			"GROUP BY c.enviroment ORDER BY SUM(COALESCE(c.consumptionValue,0) + COALESCE(c.iva,0) + COALESCE(c.service,0) + COALESCE(c.tip,0)) DESC")
	List<Object[]> aggregateByEnvironment(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

	@Query("SELECT c.consumptionOpening AS consumptionOpening, c.consumptionValue AS consumptionValue, " +
			"c.iva AS iva, c.service AS service, c.tip AS tip " +
			"FROM PartnerConsumption c WHERE c.consumptionOpening BETWEEN :from AND :to")
	List<ConsumptionRowView> findRowsInRange(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

	@Query("SELECT c.enviroment, COUNT(DISTINCT c.partner.personId) FROM PartnerConsumption c " +
			"WHERE c.consumptionOpening BETWEEN :from AND :to GROUP BY c.enviroment " +
			"ORDER BY COUNT(DISTINCT c.partner.personId) DESC")
	List<Object[]> occupancyByEnvironment(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

	@Query("SELECT MIN(c.consumptionOpening) FROM PartnerConsumption c")
	LocalDateTime findEarliestConsumption();

}