package co.unbosque.service;


import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.unbosque.model.PartnerConsumption;
import co.unbosque.model.PersonPartner;
import co.unbosque.repository.PartnerConsumptionRepository;
import co.unbosque.repository.PersonPartnerRepository;

@Service
@Transactional
public class PersonPartnerService {

    @Autowired
    private PersonPartnerRepository partnerRepo;
    
    public PersonPartnerService() {
		// TODO Auto-generated constructor stub
	}
    
    public void savePartner(PersonPartner save) {
    	partnerRepo.save(save);
    }
    
    public List<PersonPartner> getAll() {
        return partnerRepo.findAll();
    }

    public PersonPartner getById(Long id) {
        Optional<PersonPartner> found = partnerRepo.findByPersonId(id);
        if(found.isPresent()) {
        	return found.get();
        }
        return null;
    }
    
    public PersonPartner getByIdentification(String identification) {
        Optional<PersonPartner> found = partnerRepo.findByIdentification(identification);
        if(found.isPresent()) {
            return found.get();
        }
        return null;
    }

    public List<PersonPartner> getByFirstName(String firstName) {
    	List<PersonPartner> list=partnerRepo.findByFirstName(firstName);
    	if(!list.isEmpty()) {
    		return list;
    	}
        return null;
    }

    public List<PersonPartner> getBySecondName(String secondName) {
    	List<PersonPartner> list=partnerRepo.findBySecondName(secondName);
    	if(!list.isEmpty()) {
    		return list;
    	}
        return null;
    }

    public List<PersonPartner> getByShareNumber(Long shareNumber) {
    	List<PersonPartner> list=partnerRepo.findByShareNumber(shareNumber);
    	if(!list.isEmpty()) {
    		return list;
    	}
        return null;
    }

    public List<PersonPartner> getByOwnerPersonId(Long ownerId) {
    	List<PersonPartner> list=partnerRepo.findByOwnerPersonId(ownerId);
    	if(!list.isEmpty()) {
    		return list;
    	}
        return null;
    }
    
    public List<PartnerConsumption> getByComsuption(Long id) {
        Optional<PersonPartner> found = partnerRepo.findByPersonId(id);
        if(found.isPresent()) {
        	return found.get().getConsumptions();
        }
        return null;
    }
    
    public PersonPartner getTitularByEmail(String email) {
    	 Optional<PersonPartner> titular = partnerRepo.findTitularByEmail(email);
         if (!titular.isPresent()) {
        	 return null;
         }
         return titular.get();
    }
    
}
