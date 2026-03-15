package co.unbosque.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import co.unbosque.model.PartnerConsumption;

public interface PartnerConsumptionRepository extends JpaRepository<PartnerConsumption, Long> {
	
	public Optional<PartnerConsumption> findByConsumptionId(Long consumptionId);
	public List<PartnerConsumption> findByPartnerPersonId(Long personId);
	public List<PartnerConsumption> findByEnviroment(String enviroment);
	
}