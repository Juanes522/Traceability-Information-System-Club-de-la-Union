package co.edu.unbosque.service;

import java.time.LocalDateTime;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.model.Access;
import co.edu.unbosque.model.PersonPartner;
import co.edu.unbosque.repository.AccessRepository;

@Service
public class AccessService {

	private final AccessRepository accessRepo;

	public AccessService(AccessRepository accessRepo) {
		this.accessRepo = accessRepo;
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void registerPresence(PersonPartner partner) {
		if (accessRepo.findOpenAccessByPartnerId(partner.getPersonId()).isEmpty()) {
			Access access = new Access();
			access.setPartner(partner);
			access.setDateTimeAdmission(LocalDateTime.now());
			accessRepo.save(access);
		}
	}

	@Transactional
	@Scheduled(cron = "0 0 2 * * *")
	public void closeOpenAccesses() {
		accessRepo.closeOpenAccesses(LocalDateTime.now());
	}
}
