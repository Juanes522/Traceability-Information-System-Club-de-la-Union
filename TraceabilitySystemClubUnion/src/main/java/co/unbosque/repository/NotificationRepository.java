package co.unbosque.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import co.unbosque.model.Notification;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

	List<Notification> findByConsumptionConsumptionId(Long consumptionId);

    List<Notification> findByStateAndNotificationType(Character state, String notificationType);
	
	@Query("SELECT n FROM Notification n WHERE n.consumption.consumptionId = :consumptionId AND n.state = 'P' AND n.notificationType = 'CONSUMPTION_VALIDATION'")
	Optional<Notification> findPendingValidationByConsumptionId(@Param("consumptionId") Long consumptionId);
	
}
