package co.unbosque.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import co.unbosque.model.PushSubscription;

@Repository
public interface PushSubscriptionRepository extends JpaRepository<PushSubscription, Long> {

	List<PushSubscription> findByPartnerIdentification(String identification);

	Optional<PushSubscription> findByEndpoint(String endpoint);

	void deleteByEndpoint(String endpoint);
}
