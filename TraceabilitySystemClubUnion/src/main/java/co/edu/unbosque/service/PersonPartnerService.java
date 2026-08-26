package co.edu.unbosque.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.model.PartnerConsumption;
import co.edu.unbosque.model.PersonPartner;
import co.edu.unbosque.repository.PersonPartnerRepository;

@Service
@Transactional
public class PersonPartnerService {

	@Autowired
	private PersonPartnerRepository partnerRepo;

	public PersonPartnerService() {
	}

	public void savePartner(PersonPartner save) {
		partnerRepo.save(save);
	}

	public List<PersonPartner> getAll() {
		return partnerRepo.findAll();
	}

	public Page<PersonPartner> getAllPaged(Pageable pageable) {
		return partnerRepo.findAll(pageable);
	}

	public PersonPartner getById(Long id) {
		Optional<PersonPartner> found = partnerRepo.findByPersonId(id);
		if (found.isPresent()) {
			return found.get();
		}
		return null;
	}

	public PersonPartner getByIdentification(String identification) {
		Optional<PersonPartner> found = partnerRepo.findByIdentification(identification);
		if (found.isPresent()) {
			return found.get();
		}
		return null;
	}

	public PersonPartner getByEmail(String email) {
		if (email == null || email.isBlank()) {
			return null;
		}
		String target = email.trim();
		for (PersonPartner partner : partnerRepo.findAll()) {
			String[] emails = partner.getEmail();
			if (emails == null) {
				continue;
			}
			for (String candidate : emails) {
				if (candidate != null && target.equalsIgnoreCase(candidate.trim())) {
					return partner;
				}
			}
		}
		return null;
	}

	public List<PersonPartner> getByFirstName(String firstName) {
		List<PersonPartner> list = partnerRepo.findByFirstName(firstName);
		if (!list.isEmpty()) {
			return list;
		}
		return null;
	}

	public List<PersonPartner> getBySecondName(String secondName) {
		List<PersonPartner> list = partnerRepo.findBySecondName(secondName);
		if (!list.isEmpty()) {
			return list;
		}
		return null;
	}

	public List<PersonPartner> getByShareNumber(Long shareNumber) {
		List<PersonPartner> list = partnerRepo.findByShareNumber(shareNumber);
		if (!list.isEmpty()) {
			return list;
		}
		return null;
	}

	public List<PartnerConsumption> getByComsuption(Long id) {
		Optional<PersonPartner> found = partnerRepo.findByPersonId(id);
		if (found.isPresent()) {
			return found.get().getConsumptions();
		}
		return null;
	}

}
