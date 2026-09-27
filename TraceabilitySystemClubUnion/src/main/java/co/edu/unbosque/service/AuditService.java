package co.edu.unbosque.service;

import java.time.Instant;

import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import co.edu.unbosque.model.AuditEvent;
import co.edu.unbosque.model.AuditEventType;
import co.edu.unbosque.model.AuditResult;
import co.edu.unbosque.model.AuditSeverity;

/**
 * Camino de <strong>escritura</strong> de la bitácora de seguridad.
 *
 * <p>Indexa cada evento en Elasticsearch (índice {@code audit-log}) usando {@code ElasticsearchOperations}
 * directamente: no hay repositorio para {@link AuditEvent}, porque no es una entidad JPA.
 *
 * <p>El diseño está gobernado por un principio: <strong>auditar no debe poder romper la operación que se
 * audita</strong>. De ahí que ambos métodos sean {@code @Async} —el llamante no espera— y que capturen toda
 * excepción.
 *
 * <p>La contrapartida de ese principio conviene enunciarla sin rodeos: <strong>la pérdida de eventos es
 * silenciosa</strong>. Si Elasticsearch no está disponible la aplicación funciona con normalidad y los eventos
 * se descartan, incluidos los de severidad {@code CRITICAL}, sin contador, ni reintento, ni alerta, ni registro
 * estructurado. Para un subsistema de auditoría esa es la propiedad más discutible del diseño: una bitácora
 * vacía no permite concluir que no ocurrió nada.
 *
 * <p>Nótese que el camino de lectura ({@link AuditQueryService}) <strong>no</strong> traga sus excepciones, y es
 * por eso que una prueba de integración de la suite falla cuando no hay Elasticsearch en ejecución.
 *
 * @see AuditQueryService
 * @see co.edu.unbosque.model.AuditEventType
 */
@Service
public class AuditService {

	private final ElasticsearchOperations operations;

	public AuditService(ElasticsearchOperations operations) {
		this.operations = operations;
	}

	/**
	 * Registra un evento con la marca de tiempo actual.
	 *
	 * @param eventType tipo de evento, de {@link AuditEventType}
	 * @param result    desenlace, de {@link AuditResult}
	 * @param username  sujeto del evento, habitualmente la identificación del socio <strong>en claro</strong>
	 * @param ipAddress IP del cliente, resuelta por el llamante
	 * @param detail    descripción legible del evento
	 * @param targetId  identificador del objeto afectado, o {@code null}
	 */
	@Async
	public void record(String eventType, String result, String username, String ipAddress,
			String detail, String targetId) {
		record(eventType, result, username, ipAddress, detail, targetId, Instant.now());
	}

	/**
	 * Registra un evento con una marca de tiempo explícita.
	 *
	 * <p>La sobrecarga con {@code timestamp} existe para poder <strong>retrodatar</strong> un evento al momento
	 * del hecho y no al de su registro. Su único uso es el evento de cargo registrado, que se fecha en la hora de
	 * apertura del consumo.
	 *
	 * <p>La severidad no la aporta el llamante: la deriva {@link #deriveSeverity(String, String)}, lo que
	 * garantiza una clasificación uniforme en todo el sistema.
	 *
	 * <p>Sobre la IP: debe resolverla el llamante <strong>en el hilo de la petición</strong> y pasarla como
	 * parámetro. Este método se ejecuta en otro hilo, y el contexto de la petición no se propaga, de modo que
	 * consultarla aquí devolvería siempre {@code null}. Todos los puntos de llamada lo hacen correctamente.
	 *
	 * @param eventType tipo de evento, de {@link AuditEventType}
	 * @param result    desenlace, de {@link AuditResult}
	 * @param username  sujeto del evento
	 * @param ipAddress IP del cliente, resuelta por el llamante
	 * @param detail    descripción legible del evento
	 * @param targetId  identificador del objeto afectado, o {@code null}
	 * @param timestamp instante a registrar; si es {@code null} se usa el actual
	 */
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

	/**
	 * Clasifica la severidad del evento a partir de su tipo y desenlace.
	 *
	 * <p>Centralizar esta decisión —en lugar de dejar que cada llamante declare su severidad— es lo que hace
	 * comparables los eventos entre sí y permite que el panel de administración cuente alertas críticas de forma
	 * fiable.
	 *
	 * <p>Reglas, en orden de precedencia: un bloqueo por tasa o una denegación de acceso son
	 * {@link AuditSeverity#CRITICAL}; cualquier otro fallo es {@link AuditSeverity#WARNING}; el resto,
	 * {@link AuditSeverity#INFO}. Nótese que un inicio de sesión fallido queda en {@code WARNING}: es el bloqueo
	 * reiterado, no el fallo aislado, lo que se considera crítico.
	 *
	 * @param eventType tipo de evento
	 * @param result    desenlace
	 * @return la severidad derivada
	 */
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
