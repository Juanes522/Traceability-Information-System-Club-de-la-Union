package co.edu.unbosque.service;

import java.time.Instant;

import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import co.edu.unbosque.model.AuditEvent;
import co.edu.unbosque.model.AuditEventType;
import co.edu.unbosque.model.AuditResult;
import co.edu.unbosque.model.AuditSeverity;

@Service
public class AuditService {

	private final ElasticsearchOperations operations;

	public AuditService(ElasticsearchOperations operations) {
		this.operations = operations;
	}

	@Async
	public void record(String eventType, String result, String username, String ipAddress,
			String detail, String targetId) {
		record(eventType, result, username, ipAddress, detail, targetId, Instant.now());
	}

	@Async
	public void record(String eventType, String result, String username, String ipAddress,
			String detail, String targetId, Instant timestamp) {
		try {
			AuditEvent event = new AuditEvent();
			event.setTimestamp(timestamp != null ? timestamp : Instant.now());
			event.setEventType(eventType);
			event.setResult(result);
			event.setUsername(username);
			event.setIpAddress(ipAddress);
			event.setDetail(detail);
			event.setTargetId(targetId);
			event.setSeverity(deriveSeverity(eventType, result));
			operations.save(event);
		} catch (Exception e) {
			System.err.println("Audit record failed: " + e.getMessage());
		}
	}

	private String deriveSeverity(String eventType, String result) {
		if (AuditEventType.RATE_LIMIT_BLOCK.equals(eventType) || AuditEventType.ACCESS_DENIED.equals(eventType)) {
			return AuditSeverity.CRITICAL;
		}
		if (AuditResult.FAILURE.equals(result)) {
			return AuditSeverity.WARNING;
		}
		return AuditSeverity.INFO;
	}
}
