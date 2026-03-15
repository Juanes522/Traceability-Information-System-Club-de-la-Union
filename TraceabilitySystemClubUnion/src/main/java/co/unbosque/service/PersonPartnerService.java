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

    @Autowired
    private PartnerConsumptionRepository consumptionRepo;
    
    public PersonPartnerService() {
		// TODO Auto-generated constructor stub
	}
    
    @Transactional(readOnly = true)
    public List<PersonPartner> getAll() {
        return partnerRepo.findAll();
    }

    @Transactional(readOnly = true)
    public PersonPartner getById(Long id) {
        Optional<PersonPartner> found = partnerRepo.findByPersonId(id);
        if(found.isPresent()) {
        	return found.get();
        }
        return null;
    }
    
    @Transactional(readOnly = true)
    public PersonPartner getByIdentification(String identification) {
        Optional<PersonPartner> found = partnerRepo.findByIdentification(identification);
        if(found.isPresent()) {
            return found.get();
        }
        return null;
    }

    @Transactional(readOnly = true)
    public List<PersonPartner> getByFirstName(String firstName) {
    	List<PersonPartner> list=partnerRepo.findByFirstName(firstName);
    	if(!list.isEmpty()) {
    		return list;
    	}
        return null;
    }

    @Transactional(readOnly = true)
    public List<PersonPartner> getBySecondName(String secondName) {
    	List<PersonPartner> list=partnerRepo.findBySecondName(secondName);
    	if(!list.isEmpty()) {
    		return list;
    	}
        return null;
    }

    @Transactional(readOnly = true)
    public List<PersonPartner> getByShareNumber(Long shareNumber) {
    	List<PersonPartner> list=partnerRepo.findByShareNumber(shareNumber);
    	if(!list.isEmpty()) {
    		return list;
    	}
        return null;
    }

    @Transactional(readOnly = true)
    public List<PersonPartner> getByOwnerPersonId(Long ownerId) {
    	List<PersonPartner> list=partnerRepo.findByOwnerPersonId(ownerId);
    	if(!list.isEmpty()) {
    		return list;
    	}
        return null;
    }
    
    @Transactional(readOnly = true)
    public List<PartnerConsumption> getByConsuption(Long id) {
        Optional<PersonPartner> found = partnerRepo.findByPersonId(id);
        if(found.isPresent()) {
        	return found.get().getConsumptions();
        }
        return null;
    }
    
}
