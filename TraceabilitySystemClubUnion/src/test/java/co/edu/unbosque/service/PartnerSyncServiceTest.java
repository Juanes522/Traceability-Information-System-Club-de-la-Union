package co.edu.unbosque.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.client.RestClient;

import co.edu.unbosque.dto.ExternalSocioDTO;
import co.edu.unbosque.dto.SyncResultDTO;
import co.edu.unbosque.model.PersonPartner;

class PartnerSyncServiceTest {

	private final PersonPartnerService partnerService = mock(PersonPartnerService.class);
	private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
	private final PartnerSyncService service = new PartnerSyncService(partnerService, RestClient.builder(), "http://x/socios.json", passwordEncoder);

	private ExternalSocioDTO ext(String id, long share) {
		ExternalSocioDTO e = new ExternalSocioDTO();
		e.setIdentification(id); e.setFirstName("Ana"); e.setSecondName("María"); e.setLastName("López Ríos");
		e.setGender("F"); e.setShareNumber(share); e.setEmail("ana.lopez@x.com");
		e.setPhone("600000121"); e.setCellPhone("300000121");
		e.setBirthDate("1985-01-01"); e.setIngressDate("2018-01-01");
		return e;
	}

	@Test
	void upsert_creaNuevoYMapeaCampos() {
		when(partnerService.getByIdentification("9900000121")).thenReturn(null);
		when(passwordEncoder.encode("9900000121")).thenReturn("{bcrypt}TEMP");
		org.mockito.ArgumentCaptor<PersonPartner> cap = org.mockito.ArgumentCaptor.forClass(PersonPartner.class);
		SyncResultDTO r = service.upsert(List.of(ext("9900000121", 121L)));
		assertThat(r.getCreated()).isEqualTo(1L);
		assertThat(r.getUpdated()).isEqualTo(0L);
		verify(partnerService).savePartner(cap.capture());
		PersonPartner p = cap.getValue();
		assertThat(p.getShareNumber()).isEqualTo(121L);
		assertThat(p.getGender()).isEqualTo('F');
		assertThat(p.getEmail()).containsExactly("ana.lopez@x.com");
		assertThat(p.getRole()).isEqualTo("ROLE_PARTNER");
		assertThat(p.getPartnerState()).isTrue();
		assertThat(p.getPassword()).isEqualTo("{bcrypt}TEMP");
		assertThat(p.getForcePasswordChange()).isTrue();
	}

	@Test
	void upsert_noPisaPasswordDeExistente() {
		PersonPartner existente = new PersonPartner();
		existente.setIdentification("9900000121");
		existente.setPassword("{bcrypt}YA_TIENE");
		when(partnerService.getByIdentification("9900000121")).thenReturn(existente);
		service.upsert(List.of(ext("9900000121", 121L)));
		assertThat(existente.getPassword()).isEqualTo("{bcrypt}YA_TIENE");
	}

	@Test
	void upsert_actualizaExistente() {
		PersonPartner existente = new PersonPartner();
		existente.setIdentification("9900000121");
		when(partnerService.getByIdentification("9900000121")).thenReturn(existente);
		SyncResultDTO r = service.upsert(List.of(ext("9900000121", 121L)));
		assertThat(r.getCreated()).isEqualTo(0L);
		assertThat(r.getUpdated()).isEqualTo(1L);
		verify(partnerService).savePartner(existente);
	}

	@Test
	void upsert_mezclaYEmailNull() {
		when(partnerService.getByIdentification("A")).thenReturn(null);
		when(partnerService.getByIdentification("B")).thenReturn(new PersonPartner());
		ExternalSocioDTO sinEmail = ext("A", 1L); sinEmail.setEmail(null);
		SyncResultDTO r = service.upsert(List.of(sinEmail, ext("B", 2L)));
		assertThat(r.getCreated()).isEqualTo(1L);
		assertThat(r.getUpdated()).isEqualTo(1L);
		verify(partnerService, times(2)).savePartner(any());
	}
}
