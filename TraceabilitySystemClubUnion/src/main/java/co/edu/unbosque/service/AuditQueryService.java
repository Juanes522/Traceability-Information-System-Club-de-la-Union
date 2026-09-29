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

/**
 * Camino de <strong>lectura</strong> de la bitácora de seguridad.
 *
 * <p>Consulta el índice {@code audit-log} de Elasticsearch con {@code ElasticsearchOperations} y criterios
 * compuestos. Sirve cuatro necesidades distintas: la búsqueda paginada de la pantalla de auditoría, los contadores
 * del panel de seguridad, el historial de accesos propio de cada socio y el reporte PDF de seguridad.
 *
 * <p><strong>Diferencia esencial respecto de {@link AuditService}:</strong> este servicio <strong>no captura sus
 * excepciones</strong>. Si Elasticsearch no responde, el fallo propaga. Es la decisión correcta —devolver una
 * bitácora vacía en silencio sería peor que devolver un error—, pero tiene dos consecuencias prácticas:
 * {@link SecurityMetricsService} debe protegerse él mismo para poder degradar con elegancia, y una prueba de
 * integración de la suite falla cuando no hay Elasticsearch en ejecución.
 *
 * <p>Las agregaciones de este servicio se resuelven <strong>en Java y con topes fijos</strong>, no con agregaciones
 * de Elasticsearch. Véanse las advertencias de cada método.
 *
 * @see AuditService
 * @see co.edu.unbosque.model.AuditEvent
 */
@Service
public class AuditQueryService {

	private final ElasticsearchOperations operations;

	/**
	 * Crea una instancia con sus valores.
	 *
	 * @param operations el valor de operations
	 */
	public AuditQueryService(ElasticsearchOperations operations) {
		this.operations = operations;
	}

	/**
	 * Busca eventos de auditoría combinando los filtros indicados.
	 *
	 * <p>Los criterios se componen de forma <strong>conjuntiva</strong>, y cada filtro nulo o en blanco se omite: con
	 * todos los filtros vacíos la consulta devuelve <strong>todo el índice</strong> paginado. Es el comportamiento que
	 * espera la pantalla de auditoría, cuyo estado inicial es «sin filtrar».
	 *
	 * <p>Sirve dos usos con niveles de privilegio distintos: {@code GET /audit}, reservado a {@code ADMIN}, y
	 * {@code GET /personpartner/my-logins}, donde el propio socio consulta su historial de accesos. En el segundo caso
	 * el controlador <strong>fija el nombre de usuario desde el contexto de seguridad</strong>, de modo que nadie
	 * puede consultar el historial de otro.
	 *
	 * @param username sujeto a filtrar, o {@code null} para no filtrar
	 * @param eventType tipo de evento, o {@code null}
	 * @param result desenlace, o {@code null}
	 * @param from inicio del rango, inclusivo, o {@code null}
	 * @param to fin del rango, inclusivo, o {@code null}
	 * @param pageable página solicitada
	 * @return la página de eventos, con el total de coincidencias
	 */
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

	/**
	 * Cuenta los eventos de un tipo dentro de un rango.
	 *
	 * <p>Usa la operación de conteo de Elasticsearch, sin recuperar documentos, de modo que es eficiente aunque el
	 * índice sea grande.
	 *
	 * @param eventType tipo de evento a contar
	 * @param from inicio del rango, o {@code null}
	 * @param to fin del rango, o {@code null}
	 * @return el número de eventos coincidentes
	 */
	public long countByEventType(String eventType, Instant from, Instant to) {
		Criteria criteria = new Criteria("eventType").is(eventType);
		criteria = withRange(criteria, from, to);
		return operations.count(new CriteriaQuery(criteria), AuditEvent.class);
	}

	/**
	 * Cuenta los eventos de una severidad dentro de un rango.
	 *
	 * <p>Con {@link co.edu.unbosque.model.AuditSeverity#CRITICAL} produce el indicador de alertas críticas del panel de
	 * administración.
	 *
	 * @param severity severidad a contar
	 * @param from inicio del rango, o {@code null}
	 * @param to fin del rango, o {@code null}
	 * @return el número de eventos coincidentes
	 */
	public long countBySeverity(String severity, Instant from, Instant to) {
		Criteria criteria = new Criteria("severity").is(severity);
		criteria = withRange(criteria, from, to);
		return operations.count(new CriteriaQuery(criteria), AuditEvent.class);
	}

	/**
	 * Agrupa los intentos fallidos de inicio de sesión por usuario, de mayor a menor.
	 *
	 * <p>Responde a la pregunta operativa «¿qué cuentas están siendo atacadas?», y alimenta el reporte PDF de
	 * seguridad.
	 *
	 * <p><strong>Advertencia sobre la exactitud del resultado.</strong> Recupera hasta <strong>5 000</strong>
	 * documentos y los agrupa <strong>en memoria</strong>, en lugar de delegar en una agregación {@code terms} de
	 * Elasticsearch. Pasado ese tope el recuento <strong>subestima sin avisar</strong>, y es justo durante un incidente
	 * —cuando estas cifras importan— cuando es más probable superarlo.
	 *
	 * <p>Los eventos sin usuario identificable se agrupan bajo una etiqueta genérica en lugar de descartarse.
	 *
	 * @param from inicio del rango, o {@code null}
	 * @param to fin del rango, o {@code null}
	 * @return los usuarios con fallos y su recuento, ordenados de mayor a menor
	 */
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

	/**
	 * Devuelve los eventos críticos del rango, del más reciente al más antiguo.
	 *
	 * <p>Alimenta la tabla de alertas del reporte PDF de seguridad. Está acotado a <strong>500</strong> documentos: un
	 * tope pensado para que el PDF sea manejable, pero que también significa que en un periodo con muchos incidentes el
	 * reporte muestra solo los más recientes, sin indicar que hubo más.
	 *
	 * @param from inicio del rango, o {@code null}
	 * @param to fin del rango, o {@code null}
	 * @return los eventos de severidad crítica, ordenados por fecha descendente
	 */
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
