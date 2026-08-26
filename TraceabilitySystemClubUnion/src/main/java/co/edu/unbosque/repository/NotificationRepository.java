package co.edu.unbosque.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import co.edu.unbosque.model.Notification;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

	List<Notification> findByConsumptionConsumptionId(Long consumptionId);

	List<Notification> findByConsumptionPartnerIdentificationOrderByGenerationDateDesc(String identification);

	Page<Notification> findByConsumptionPartnerIdentificationOrderByGenerationDateDesc(
			String identification, Pageable pageable);
}
