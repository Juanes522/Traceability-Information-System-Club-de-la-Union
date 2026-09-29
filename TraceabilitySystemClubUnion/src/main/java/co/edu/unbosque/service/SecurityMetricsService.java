package co.edu.unbosque.service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.springframework.stereotype.Service;

import co.edu.unbosque.dto.SecuritySummaryDTO;
import co.edu.unbosque.model.AuditEventType;
import co.edu.unbosque.model.AuditSeverity;

/**
 * Indicadores del panel de seguridad, calculados sobre la bitácora de auditoría.
 *
 * <p>Es el único servicio de métricas que no consulta SQL Server: agrega contando documentos en Elasticsearch a
 * través de {@link AuditQueryService}. Lo consume {@code GET /metrics/security/summary}, reservado al rol
 * {@code ADMIN}.
 *
 * @see co.edu.unbosque.dto.SecuritySummaryDTO
 */
@Service
public class SecurityMetricsService {

	private final AuditQueryService auditQueryService;

	/**
	 * Crea una instancia con sus valores.
	 *
	 * @param auditQueryService el valor de audit query service
	 */
	public SecurityMetricsService(AuditQueryService auditQueryService) {
		this.auditQueryService = auditQueryService;
	}

	/**
	 * Calcula los cuatro indicadores de seguridad del rango: inicios de sesión fallidos, bloqueos por tasa,
	 * accesos denegados y alertas críticas.
	 *
	 * <p><strong>Degradación elegante deliberada.</strong> Si Elasticsearch no responde, el método no propaga la
	 * excepción: marca el DTO como {@code degraded} y devuelve los contadores que alcanzó a calcular, de modo que
	 * el endpoint responde 200 y el panel de administración sigue siendo utilizable en lugar de quedarse en
	 * blanco.
	 *
	 * <p>El indicador {@code degraded} es <strong>la única señal</strong> de que los números no son fiables, y el
	 * cliente debe mostrarlo: sin él, unos contadores en cero parecerían indicar ausencia de incidentes cuando en
	 * realidad indican ausencia de datos.
	 *
	 * <p>Nótese que la degradación es parcial y ordenada: si el fallo ocurre a mitad de los cuatro conteos, los
	 * primeros conservan su valor real y los restantes quedan en cero, pero el indicador ya está activo.
	 *
	 * <p>Las fechas se convierten a instante usando la zona horaria del sistema, criterio que este servicio
	 * comparte con {@link ReportService} para su reporte de seguridad.
	 *
	 * @param from inicio del rango, o {@code null} para no acotar
	 * @param to fin del rango, o {@code null} para no acotar
	 * @return los indicadores; con {@code degraded} activo si la bitácora no estaba disponible
	 */
	public SecuritySummaryDTO summary(LocalDateTime from, LocalDateTime to) {
		Instant fromI = toInstant(from);
		Instant toI = toInstant(to);
		SecuritySummaryDTO dto = new SecuritySummaryDTO();
		try {
			dto.setLoginFailedCount(auditQueryService.countByEventType(AuditEventType.LOGIN_FAILED, fromI, toI));
			dto.setRateLimitBlockCount(auditQueryService.countByEventType(AuditEventType.RATE_LIMIT_BLOCK, fromI, toI));
			dto.setAccessDeniedCount(auditQueryService.countByEventType(AuditEventType.ACCESS_DENIED, fromI, toI));
			dto.setCriticalAlertCount(auditQueryService.countBySeverity(AuditSeverity.CRITICAL, fromI, toI));
		} catch (Exception e) {
			dto.setDegraded(true);
			System.err.println("Security metrics unavailable: " + e.getMessage());
		}
		return dto;
	}

	private Instant toInstant(LocalDateTime dt) {
		return dt == null ? null : dt.atZone(ZoneId.systemDefault()).toInstant();
	}
}
