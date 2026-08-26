package co.edu.unbosque.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.stereotype.Service;

import co.edu.unbosque.dto.UserFailedCountDTO;
import co.edu.unbosque.model.AuditEvent;
import co.edu.unbosque.model.AuditEventType;
import co.edu.unbosque.model.AuditSeverity;

@Service
public class AuditQueryService {

	private final ElasticsearchOperations operations;

	public AuditQueryService(ElasticsearchOperations operations) {
		this.operations = operations;
	}

	public Page<AuditEvent> search(String username, String eventType, String result,
			Instant from, Instant to, Pageable pageable) {
		Criteria criteria = new Criteria();
		if (username != null && !username.isBlank()) {
			criteria = criteria.and(new Criteria("username").is(username));
		}
		if (eventType != null && !eventType.isBlank()) {
			criteria = criteria.and(new Criteria("eventType").is(eventType));
		}
		if (result != null && !result.isBlank()) {
			criteria = criteria.and(new Criteria("result").is(result));
		}
		if (from != null) {
			criteria = criteria.and(new Criteria("timestamp").greaterThanEqual(from));
		}
		if (to != null) {
			criteria = criteria.and(new Criteria("timestamp").lessThanEqual(to));
		}

		CriteriaQuery query = new CriteriaQuery(criteria, pageable);
		SearchHits<AuditEvent> hits = operations.search(query, AuditEvent.class);
		List<AuditEvent> content = hits.stream().map(SearchHit::getContent).toList();
		return new PageImpl<>(content, pageable, hits.getTotalHits());
	}

	public long countByEventType(String eventType, Instant from, Instant to) {
		Criteria criteria = new Criteria("eventType").is(eventType);
		criteria = withRange(criteria, from, to);
		return operations.count(new CriteriaQuery(criteria), AuditEvent.class);
	}

	public long countBySeverity(String severity, Instant from, Instant to) {
		Criteria criteria = new Criteria("severity").is(severity);
		criteria = withRange(criteria, from, to);
		return operations.count(new CriteriaQuery(criteria), AuditEvent.class);
	}

	public List<UserFailedCountDTO> failedLoginsByUser(Instant from, Instant to) {
		Criteria criteria = new Criteria("eventType").is(AuditEventType.LOGIN_FAILED);
		criteria = withRange(criteria, from, to);
		CriteriaQuery query = new CriteriaQuery(criteria, PageRequest.of(0, 5000));
		SearchHits<AuditEvent> hits = operations.search(query, AuditEvent.class);
		Map<String, Long> counts = new HashMap<>();
		for (SearchHit<AuditEvent> hit : hits) {
			String user = hit.getContent().getUsername();
			String key = user == null ? "(desconocido)" : user;
			counts.merge(key, 1L, Long::sum);
		}
		List<UserFailedCountDTO> result = new ArrayList<>();
		for (Map.Entry<String, Long> e : counts.entrySet()) {
			result.add(new UserFailedCountDTO(e.getKey(), e.getValue()));
		}
		result.sort(Comparator.comparingLong(UserFailedCountDTO::getCount).reversed());
		return result;
	}

	public List<AuditEvent> criticalEvents(Instant from, Instant to) {
		Criteria criteria = new Criteria("severity").is(AuditSeverity.CRITICAL);
		criteria = withRange(criteria, from, to);
		CriteriaQuery query = new CriteriaQuery(criteria,
				PageRequest.of(0, 500, Sort.by(Sort.Direction.DESC, "timestamp")));
		SearchHits<AuditEvent> hits = operations.search(query, AuditEvent.class);
		List<AuditEvent> result = new ArrayList<>();
		for (SearchHit<AuditEvent> hit : hits) {
			result.add(hit.getContent());
		}
		return result;
	}

	private Criteria withRange(Criteria criteria, Instant from, Instant to) {
		if (from != null) {
			criteria = criteria.and(new Criteria("timestamp").greaterThanEqual(from));
		}
		if (to != null) {
			criteria = criteria.and(new Criteria("timestamp").lessThanEqual(to));
		}
		return criteria;
	}
}
