package co.edu.unbosque.controller;

import co.edu.unbosque.dto.ProductRankDTO;
import co.edu.unbosque.model.PersonPartner;
import co.edu.unbosque.service.PersonPartnerService;
import co.edu.unbosque.service.ProductMetricsService;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProductMetricsControllerTest {

	private ProductMetricsService metrics;
	private PersonPartnerService partnerService;
	private ProductMetricsController controller;
	private final LocalDateTime from = LocalDateTime.of(2026, 8, 1, 0, 0);
	private final LocalDateTime to = LocalDateTime.of(2026, 8, 31, 0, 0);

	@BeforeEach
	void setUp() {
		metrics = mock(ProductMetricsService.class);
		partnerService = mock(PersonPartnerService.class);
		controller = new ProductMetricsController(metrics, partnerService);
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
	void top_returnsOk() {
		when(metrics.top(any(), any(), any(), any(), anyInt())).thenReturn(List.of(new ProductRankDTO("P1", "VINO", 3L, 30.0)));
		ResponseEntity<?> r = controller.top(from, to, null, "revenue", 20);
		assertEquals(HttpStatus.OK, r.getStatusCode());
	}

	@Test
	void top_conEnvironment_pasaElParametro() {
		when(metrics.top(any(), any(), eq("Rooftop"), any(), anyInt()))
				.thenReturn(List.of(new ProductRankDTO("PX", "COCTEL", 5L, 40.0)));
		ResponseEntity<?> r = controller.top(from, to, "Rooftop", "revenue", 20);
		assertEquals(HttpStatus.OK, r.getStatusCode());
		assertEquals(List.of(new ProductRankDTO("PX", "COCTEL", 5L, 40.0)).size(), ((List<?>) r.getBody()).size());
		verify(metrics).top(from, to, "Rooftop", "revenue", 20);
	}

	@Test
	void top_conSortQuantity_pasaElParametro() {
		when(metrics.top(any(), any(), any(), eq("quantity"), anyInt()))
				.thenReturn(List.of(new ProductRankDTO("PY", "CERVEZA", 10L, 20.0)));
		ResponseEntity<?> r = controller.top(from, to, null, "quantity", 20);
		assertEquals(HttpStatus.OK, r.getStatusCode());
		verify(metrics).top(from, to, null, "quantity", 20);
	}

	@Test
	void partnerByIdentification_notFoundWhenMissing() {
		when(partnerService.getByIdentification("nope")).thenReturn(null);
		ResponseEntity<?> r = controller.partner("nope", from, to, 20);
		assertEquals(HttpStatus.NOT_FOUND, r.getStatusCode());
	}

	@Test
	void partnerByIdentification_returnsOkForExistingPartner() {
		when(partnerService.getByIdentification("123")).thenReturn(partner());
		when(metrics.topByPartner(anyLong(), any(), any(), anyInt())).thenReturn(List.of());
		ResponseEntity<?> r = controller.partner("123", from, to, 20);
		assertEquals(HttpStatus.OK, r.getStatusCode());
	}

	@Test
	void me_routesToMeHandlerNotIdentificationHandler() {
		UserDetails principal = User.withUsername("123").password("x").authorities("ROLE_PARTNER").build();
		SecurityContextHolder.getContext().setAuthentication(
				new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
		when(partnerService.getByIdentification("123")).thenReturn(partner());
		when(metrics.topByPartner(anyLong(), any(), any(), anyInt())).thenReturn(List.of());

		ResponseEntity<?> r = controller.me(from, to, 20);

		assertEquals(HttpStatus.OK, r.getStatusCode());
		verify(partnerService, never()).getByIdentification("me");
	}

	@Test
	void me_unauthorizedWithoutAuthentication() {
		SecurityContextHolder.clearContext();
		ResponseEntity<?> r = controller.me(from, to, 20);
		assertEquals(HttpStatus.UNAUTHORIZED, r.getStatusCode());
	}
}
