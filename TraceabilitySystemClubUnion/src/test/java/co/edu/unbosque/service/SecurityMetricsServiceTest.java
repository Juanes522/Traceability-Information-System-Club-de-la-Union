package co.edu.unbosque.service;

import co.edu.unbosque.dto.SecuritySummaryDTO;
import co.edu.unbosque.model.AuditEventType;
import co.edu.unbosque.model.AuditSeverity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Verifica los contadores de seguridad y su degradación elegante.
 *
 * <p>Su caso central comprueba que, cuando la bitácora no responde, el servicio <strong>marca el resultado como degradado en
 * lugar de fallar</strong>. Y el caso complementario verifica que no lo marca cuando todo funciona, sin lo cual el indicador
 * sería inútil por estar siempre activo.
 */
class SecurityMetricsServiceTest {

	private AuditQueryService auditQuery;
	private SecurityMetricsService service;
	private final LocalDateTime from = LocalDateTime.of(2026, 8, 1, 0, 0);
	private final LocalDateTime to = LocalDateTime.of(2026, 8, 31, 0, 0);

	@BeforeEach
	void setUp() {
		auditQuery = mock(AuditQueryService.class);
		service = new SecurityMetricsService(auditQuery);
	}

	@Test
	void summary_collectsCountsPerEventTypeAndSeverity() {
		when(auditQuery.countByEventType(eq(AuditEventType.LOGIN_FAILED), any(), any())).thenReturn(5L);
		when(auditQuery.countByEventType(eq(AuditEventType.RATE_LIMIT_BLOCK), any(), any())).thenReturn(2L);
		when(auditQuery.countByEventType(eq(AuditEventType.ACCESS_DENIED), any(), any())).thenReturn(1L);
		when(auditQuery.countBySeverity(eq(AuditSeverity.CRITICAL), any(), any())).thenReturn(3L);

		SecuritySummaryDTO dto = service.summary(from, to);

		assertEquals(5L, dto.getLoginFailedCount());
		assertEquals(2L, dto.getRateLimitBlockCount());
		assertEquals(1L, dto.getAccessDeniedCount());
		assertEquals(3L, dto.getCriticalAlertCount());
	}

	@Test
	void summary_marksDegradedWhenElasticFails() {
		when(auditQuery.countByEventType(org.mockito.ArgumentMatchers.anyString(), any(), any()))
				.thenThrow(new RuntimeException("ES down"));

		SecuritySummaryDTO dto = service.summary(from, to);

		org.junit.jupiter.api.Assertions.assertTrue(dto.isDegraded());
		assertEquals(0L, dto.getLoginFailedCount());
	}

	@Test
	void summary_notDegradedOnSuccess() {
		when(auditQuery.countByEventType(org.mockito.ArgumentMatchers.anyString(), any(), any())).thenReturn(0L);
		when(auditQuery.countBySeverity(org.mockito.ArgumentMatchers.anyString(), any(), any())).thenReturn(0L);

		SecuritySummaryDTO dto = service.summary(from, to);

		org.junit.jupiter.api.Assertions.assertFalse(dto.isDegraded());
	}
}
