package co.unbosque.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import co.unbosque.model.ConsumptionValidation;

@Repository
public interface ConsumptionValidationRepository extends JpaRepository<ConsumptionValidation, Long> {

	Optional<ConsumptionValidation> findByConsumptionConsumptionId(Long consumptionId);
	
}
