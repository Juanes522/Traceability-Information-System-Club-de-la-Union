package co.edu.unbosque.service;

import co.edu.unbosque.model.Access;
import co.edu.unbosque.model.PersonPartner;
import co.edu.unbosque.repository.AccessRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

class AccessServiceTest {

	private AccessRepository repo;
	private AccessService service;

	@BeforeEach
	void setUp() {
		repo = mock(AccessRepository.class);
		service = new AccessService(repo);
	}

	private PersonPartner partner() {
		PersonPartner p = new PersonPartner();
		ReflectionTestUtils.setField(p, "personId", 7L);
		return p;
	}

	@Test
	void registerPresence_createsWhenNoOpenAccess() {
		when(repo.findOpenAccessByPartnerId(7L)).thenReturn(Optional.empty());
		service.registerPresence(partner());
		verify(repo, times(1)).save(any(Access.class));
	}

	@Test
	void registerPresence_skipsWhenOpenAccessExists() {
		when(repo.findOpenAccessByPartnerId(7L)).thenReturn(Optional.of(new Access()));
		service.registerPresence(partner());
		verify(repo, never()).save(any(Access.class));
	}

	@Test
	void registerPresence_usesProvidedAdmissionTime() {
		when(repo.findOpenAccessByPartnerId(7L)).thenReturn(Optional.empty());
		LocalDateTime when = LocalDateTime.of(2026, 5, 15, 11, 45, 0);

		service.registerPresence(partner(), when);

		ArgumentCaptor<Access> captor = ArgumentCaptor.forClass(Access.class);
		verify(repo).save(captor.capture());
		assertEquals(when, captor.getValue().getDateTimeAdmission());
	}

	@Test
	void closeOpenAccesses_invokesBulkUpdate() {
		service.closeOpenAccesses();
		verify(repo, times(1)).closeOpenAccesses(any());
	}
}
