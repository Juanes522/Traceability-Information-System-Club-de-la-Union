package co.edu.unbosque.service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.springframework.stereotype.Service;

import co.edu.unbosque.dto.SecuritySummaryDTO;
import co.edu.unbosque.model.AuditEventType;
import co.edu.unbosque.model.AuditSeverity;

@Service
public class SecurityMetricsService {

	private final AuditQueryService auditQueryService;

	public SecurityMetricsService(AuditQueryService auditQueryService) {
		this.auditQueryService = auditQueryService;
	}

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
