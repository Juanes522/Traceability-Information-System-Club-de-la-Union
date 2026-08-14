package co.unbosque.service;

import java.time.Instant;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.stereotype.Service;

import co.unbosque.model.AuditEvent;

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
}
