package co.edu.unbosque.controller;

import co.edu.unbosque.dto.PartnerMetricsDTO;
import co.edu.unbosque.model.PersonPartner;
import co.edu.unbosque.service.PartnerMetricsService;
import co.edu.unbosque.service.PersonPartnerService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PartnerMetricsControllerTest {

	private PartnerMetricsService metrics;
	private PersonPartnerService partnerService;
	private PartnerMetricsController controller;
	private final LocalDateTime from = LocalDateTime.of(2026, 8, 1, 0, 0);
	private final LocalDateTime to = LocalDateTime.of(2026, 8, 31, 0, 0);

	@BeforeEach
	void setUp() {
		metrics = mock(PartnerMetricsService.class);
		partnerService = mock(PersonPartnerService.class);
		controller = new PartnerMetricsController(metrics, partnerService);
	}

	@AfterEach
	void clear() {
		SecurityContextHolder.clearContext();
	}

	private PersonPartner partner() {
		PersonPartner p = new PersonPartner();
		p.setIdentification("123");
		ReflectionTestUtils.setField(p, "personId", 7L);
		return p;
	}

	@Test
	void me_returnsMetricsForAuthenticatedPartner() {
		UserDetails principal = User.withUsername("123").password("x").authorities("ROLE_PARTNER").build();
		SecurityContextHolder.getContext().setAuthentication(
				new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
		when(partnerService.getByIdentification("123")).thenReturn(partner());
		when(metrics.forPartner(any(), any(), any(), any())).thenReturn(new PartnerMetricsDTO());

		ResponseEntity<?> r = controller.me(from, to, "day");
		assertEquals(HttpStatus.OK, r.getStatusCode());
	}

	@Test
	void me_unauthorizedWithoutAuthentication() {
		SecurityContextHolder.clearContext();
		ResponseEntity<?> r = controller.me(from, to, "day");
		assertEquals(HttpStatus.UNAUTHORIZED, r.getStatusCode());
	}

	@Test
	void byIdentification_returnsOkForExistingPartner() {
		when(partnerService.getByIdentification("123")).thenReturn(partner());
		when(metrics.forPartner(any(), any(), any(), any())).thenReturn(new PartnerMetricsDTO());
		ResponseEntity<?> r = controller.byIdentification("123", from, to, "day");
		assertEquals(HttpStatus.OK, r.getStatusCode());
	}

	@Test
	void byIdentification_notFoundWhenMissing() {
		when(partnerService.getByIdentification("999")).thenReturn(null);
		ResponseEntity<?> r = controller.byIdentification("999", from, to, "day");
		assertEquals(HttpStatus.NOT_FOUND, r.getStatusCode());
	}

	@Test
	void byIdentification_rejectsInvalidRange() {
		ResponseEntity<?> r = controller.byIdentification("123",
				LocalDateTime.of(2026, 8, 31, 0, 0), LocalDateTime.of(2026, 8, 1, 0, 0), "day");
		assertEquals(HttpStatus.BAD_REQUEST, r.getStatusCode());
	}
}
