package co.unbosque.repository;

import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import co.unbosque.model.RevokedToken;

@Repository
public interface RevokedTokenRepository extends JpaRepository<RevokedToken, Long> {

	boolean existsByJti(String jti);

	@Modifying
	@Transactional
	void deleteByExpiryDateBefore(LocalDateTime cutoff);
}
