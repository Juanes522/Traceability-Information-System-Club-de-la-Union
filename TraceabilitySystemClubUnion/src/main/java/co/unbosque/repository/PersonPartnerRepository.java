package co.unbosque.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import co.unbosque.model.PersonPartner;

@Repository
public interface PersonPartnerRepository extends JpaRepository<PersonPartner, Long> {

	Optional<PersonPartner> findByPersonId(Long personId);

	Optional<PersonPartner> findByIdentification(String identification);

	List<PersonPartner> findByFirstName(String firstName);

	List<PersonPartner> findBySecondName(String secondName);

	List<PersonPartner> findByShareNumber(Long shareNumber);

	@Query("SELECT p FROM PersonPartner p WHERE p.email LIKE CONCAT('%', :email, '%')")
	Optional<PersonPartner> findByEmailLike(@Param("email") String email);
}
