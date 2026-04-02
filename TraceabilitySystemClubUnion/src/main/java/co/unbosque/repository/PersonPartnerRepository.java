package co.unbosque.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import co.unbosque.model.PersonPartner;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface PersonPartnerRepository extends JpaRepository<PersonPartner, Long> {

    Optional<PersonPartner> findByPersonId(Long personId);

    Optional<PersonPartner> findByIdentification(String identification);
    
    List<PersonPartner> findByFirstName(String firstName);
    
    List<PersonPartner> findBySecondName(String secondName);
    
    List<PersonPartner> findByShareNumber(Long shareNumber);

    List<PersonPartner> findByOwnerPersonId(Long ownerId);
    
    @Query(value = "SELECT * FROM person_partner WHERE email LIKE %:email% AND owner_id IS NULL", nativeQuery = true)
    Optional<PersonPartner> findTitularByEmail(@Param("email") String email);
}

