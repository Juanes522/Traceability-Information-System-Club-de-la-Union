package co.edu.unbosque.controller;

import co.edu.unbosque.model.PersonPartner;
import co.edu.unbosque.service.PersonPartnerService;
import co.edu.unbosque.service.ReportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReportControllerTest {

	private ReportService reportService;
	private PersonPartnerService partnerService;
	private ReportController controller;
	private final byte[] PDF = new byte[]{'%', 'P', 'D', 'F', '-'};
	private static final LocalDateTime FROM = LocalDateTime.of(2026, 8, 1, 0, 0);
	private static final LocalDateTime TO = LocalDateTime.of(2026, 8, 31, 0, 0);

	@BeforeEach
	void setUp() {
		reportService = mock(ReportService.class);
		partnerService = mock(PersonPartnerService.class);
		controller = new ReportController(reportService, partnerService);
	}

	@Test
	void consumptions_returnsPdfWithHeaders() {
		when(reportService.consumptionsPdf(any(), any(), any())).thenReturn(PDF);
		ResponseEntity<?> r = controller.consumptions(
				LocalDateTime.of(2026, 8, 1, 0, 0), LocalDateTime.of(2026, 8, 31, 0, 0), null);
		assertEquals(HttpStatus.OK, r.getStatusCode());
		assertEquals(MediaType.APPLICATION_PDF, r.getHeaders().getContentType());
		assertTrue(r.getHeaders().getFirst("Content-Disposition").contains("attachment"));
	}

	@Test
	void consumptions_rejectsInvalidRange() {
		ResponseEntity<?> r = controller.consumptions(
				LocalDateTime.of(2026, 8, 31, 0, 0), LocalDateTime.of(2026, 8, 1, 0, 0), null);
		assertEquals(HttpStatus.BAD_REQUEST, r.getStatusCode());
	}

	@Test
	void partnerStatement_returns404WhenServiceReturnsNull() {
		when(reportService.partnerStatementPdf(eq("999"), any(), any())).thenReturn(null);
		ResponseEntity<?> r = controller.partnerStatement("999", null,
				LocalDateTime.of(2026, 8, 1, 0, 0), LocalDateTime.of(2026, 8, 31, 0, 0));
		assertEquals(HttpStatus.NOT_FOUND, r.getStatusCode());
	}

	@Test
	void partnerStatement_porShareNumber_resuelveTitularYGenera() {
		PersonPartner titular = new PersonPartner();
		titular.setIdentification("0001");
		when(partnerService.getByShareNumber(121L)).thenReturn(List.of(titular));
		when(reportService.partnerStatementPdf(eq("0001"), any(), any())).thenReturn(PDF);
		ResponseEntity<?> resp = controller.partnerStatement(null, 121L, FROM, TO);
		assertEquals(HttpStatus.OK, resp.getStatusCode());
		verify(reportService).partnerStatementPdf(eq("0001"), any(), any());
	}

	@Test
	void partnerStatement_shareNumberInexistente_404() {
		when(partnerService.getByShareNumber(999L)).thenReturn(List.of());
		ResponseEntity<?> resp = controller.partnerStatement(null, 999L, FROM, TO);
		assertEquals(HttpStatus.NOT_FOUND, resp.getStatusCode());
	}

	@Test
	void partnerStatement_sinCedulaNiAccion_400() {
		ResponseEntity<?> resp = controller.partnerStatement(null, null, FROM, TO);
		assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
	}

	@Test
	void security_returnsPdf() {
		when(reportService.securityPdf(any(), any())).thenReturn(PDF);
		ResponseEntity<?> r = controller.security(
				LocalDateTime.of(2026, 8, 1, 0, 0), LocalDateTime.of(2026, 8, 31, 0, 0));
		assertEquals(HttpStatus.OK, r.getStatusCode());
		assertEquals(MediaType.APPLICATION_PDF, r.getHeaders().getContentType());
	}
}
