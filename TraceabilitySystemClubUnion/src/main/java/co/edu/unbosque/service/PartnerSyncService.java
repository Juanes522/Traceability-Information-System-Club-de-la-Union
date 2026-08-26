package co.edu.unbosque.service;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import co.edu.unbosque.dto.ExternalSocioDTO;
import co.edu.unbosque.dto.SyncResultDTO;
import co.edu.unbosque.model.PersonPartner;

@Service
public class PartnerSyncService {

	private final PersonPartnerService partnerService;
	private final RestClient restClient;
	private final String externalSociosUrl;
	private final PasswordEncoder passwordEncoder;

	public PartnerSyncService(PersonPartnerService partnerService, RestClient.Builder restClientBuilder,
			@Value("${external.socios.url}") String externalSociosUrl, PasswordEncoder passwordEncoder) {
		this.partnerService = partnerService;
		this.restClient = restClientBuilder.build();
		this.externalSociosUrl = externalSociosUrl;
		this.passwordEncoder = passwordEncoder;
	}

	public SyncResultDTO sync() {
		ExternalSocioDTO[] externos = restClient.get().uri(externalSociosUrl).retrieve().body(ExternalSocioDTO[].class);
		return upsert(externos == null ? List.of() : Arrays.asList(externos));
	}

	public SyncResultDTO upsert(List<ExternalSocioDTO> externos) {
		long created = 0;
		long updated = 0;
		for (ExternalSocioDTO e : externos) {
			PersonPartner p = partnerService.getByIdentification(e.getIdentification());
			if (p == null) {
				p = new PersonPartner();
				p.setRole("PARTNER");
				p.setPartnerState(true);
				p.setPassword(passwordEncoder.encode(e.getIdentification()));
				p.setForcePasswordChange(true);
				created++;
			} else {
				updated++;
			}
			p.setIdentification(e.getIdentification());
			p.setFirstName(e.getFirstName());
			p.setSecondName(e.getSecondName());
			p.setLastName(e.getLastName());
			if (e.getGender() != null && !e.getGender().isBlank()) {
				p.setGender(e.getGender().charAt(0));
			}
			p.setShareNumber(e.getShareNumber());
			if (e.getEmail() != null) {
				p.setEmail(new String[]{ e.getEmail() });
			}
			p.setPhone(e.getPhone());
			p.setCellPhone(e.getCellPhone());
			p.setBirthDate(parseDate(e.getBirthDate()));
			p.setIngressDate(parseDate(e.getIngressDate()));
			partnerService.savePartner(p);
		}
		return new SyncResultDTO(created, updated);
	}

	private LocalDate parseDate(String s) {
		try {
			return (s == null || s.isBlank()) ? null : LocalDate.parse(s);
		} catch (Exception ex) {
			return null;
		}
	}
}
