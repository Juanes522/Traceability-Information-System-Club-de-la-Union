package co.unbosque.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import co.unbosque.model.Access;

@Repository
public interface AccessRepository extends JpaRepository<Access, Long> {

    List<Access> findByPartnerPersonIdOrderByDateTimeAdmissionDesc(Long personId);

    @Query("SELECT a FROM Access a WHERE a.partner.personId = :personId AND a.dateTimeDeparture IS NULL ORDER BY a.dateTimeAdmission DESC")
    Optional<Access> findOpenAccessByPartnerId(@Param("personId") Long personId);

    @Query("SELECT COUNT(a) > 0 FROM Access a WHERE a.partner.personId = :personId AND a.dateTimeDeparture IS NULL")
    boolean isPartnerCurrentlyPresent(@Param("personId") Long personId);

    List<Access> findByPartnerPersonIdAndDateTimeAdmissionBetween(
            Long personId, LocalDateTime from, LocalDateTime to);
}
