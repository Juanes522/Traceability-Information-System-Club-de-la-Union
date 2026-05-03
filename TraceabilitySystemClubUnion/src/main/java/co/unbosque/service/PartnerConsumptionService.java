package co.unbosque.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.unbosque.dto.ConsumptionCreateRequest;
import co.unbosque.model.Notification;
import co.unbosque.model.PartnerConsumption;
import co.unbosque.model.PersonPartner;
import co.unbosque.repository.NotificationRepository;
import co.unbosque.repository.PartnerConsumptionRepository;
import co.unbosque.repository.PersonPartnerRepository;

@Service
@Transactional
public class PartnerConsumptionService {

	@Autowired
	private PartnerConsumptionRepository consumptionRepo;

	@Autowired
	private PersonPartnerRepository partnerRepo;

	@Autowired
	private NotificationRepository notRepo;

	public PartnerConsumptionService() {
	}

	public PartnerConsumption register(ConsumptionCreateRequest req) {
		if (req.getPartnerId() == null) {
			throw new RuntimeException("Partner id is null");
		}
		if (!partnerRepo.existsById(req.getPartnerId())) {
			throw new RuntimeException("Partner id not found");
		}
		PersonPartner partner = partnerRepo.findByPersonId(req.getPartnerId()).get();

		PartnerConsumption consumption = new PartnerConsumption();
		consumption.setPartner(partner);
		consumption.setEnviroment(req.getEnviroment());
		consumption.setAccount(req.getAccount());
		consumption.setTable(req.getTable());
		consumption.setWaiterName(req.getWaiterName());
		consumption.setIsPartner(req.getIsPartner());
		consumption.setConsumptionValue(req.getConsumptionValue());
		consumption.setIva(req.getIva());
		consumption.setService(req.getService());
		consumption.setTip(req.getTip());
		consumption.setConsumptionOpening(
				req.getConsumptionOpening() != null ? req.getConsumptionOpening().atStartOfDay() : LocalDateTime.now());
		return consumptionRepo.save(consumption);
	}

	public Notification sendValidationNotification(Long consumptionId) {
		PartnerConsumption consumption = consumptionRepo.findByConsumptionId(consumptionId)
				.orElseThrow(() -> new RuntimeException("Consumption not found with id: " + consumptionId));

		Notification notification = new Notification();
		notification.setConsumption(consumption);
		notification.setNotificationType("CONSUMPTION_VALIDATION");
		notification.setGenerationDate(LocalDateTime.now().toLocalDate());
		notification.setState('P');
		return notRepo.save(notification);
	}

	public List<PartnerConsumption> getByEnviroment(String enviroment) {
		List<PartnerConsumption> list = consumptionRepo.findByEnviroment(enviroment);
		if (!list.isEmpty())
			return list;
		return null;
	}

	public List<PartnerConsumption> getByPartnerId(Long partnerPersonId) {
		List<PartnerConsumption> list = consumptionRepo.findByPartnerPersonId(partnerPersonId);
		if (!list.isEmpty())
			return list;
		return null;
	}

	public List<Notification> getNotificationsByConsumptionId(Long consumptionId) {
		Optional<PartnerConsumption> opt = consumptionRepo.findByConsumptionId(consumptionId);
		if (!opt.isPresent())
			return null;
		List<Notification> list = opt.get().getNotifications();
		if (list == null || list.isEmpty())
			return null;
		return list;
	}

	public List<PartnerConsumption> getConsumptionsWithPendingValidation() {
		List<Notification> pending = notRepo.findByStateAndNotificationType('P', "CONSUMPTION_VALIDATION");
		if (pending == null || pending.isEmpty())
			return null;
		List<PartnerConsumption> consumptions = pending.stream().map(Notification::getConsumption)
				.filter(c -> c != null).distinct().collect(Collectors.toList());
		if (consumptions.isEmpty())
			return null;
		return consumptions;
	}


}
