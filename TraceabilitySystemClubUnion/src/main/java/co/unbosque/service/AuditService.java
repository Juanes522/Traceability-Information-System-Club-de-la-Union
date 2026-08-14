package co.unbosque.service;

import java.time.Instant;

import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import co.unbosque.model.AuditEvent;

@Service
public class AuditService {

	private final ElasticsearchOperations operations;

	public AuditService(ElasticsearchOperations operations) {
		this.operations = operations;
	}

	@Async
	public void record(String eventType, String result, String username, String ipAddress,
			String detail, String targetId) {
		try {
			AuditEvent event = new AuditEvent();
			event.setTimestamp(Instant.now());
			event.setEventType(eventType);
			event.setResult(result);
			event.setUsername(username);
			event.setIpAddress(ipAddress);
			event.setDetail(detail);
			event.setTargetId(targetId);
			operations.save(event);
		} catch (Exception e) {
			System.err.println("Audit record failed: " + e.getMessage());
		}
	}
}
