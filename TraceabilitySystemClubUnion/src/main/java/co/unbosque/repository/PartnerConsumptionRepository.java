package co.unbosque.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import co.unbosque.model.PartnerConsumption;

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

}