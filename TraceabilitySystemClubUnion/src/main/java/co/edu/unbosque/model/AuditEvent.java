package co.edu.unbosque.model;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

/**
 * Evento de la bitácora de seguridad.
 *
 * <p><strong>No es una entidad JPA.</strong> Es un documento de Spring Data Elasticsearch, indexado
 * en {@code audit-log}, y es el único elemento del modelo que no vive en SQL Server. Por eso no
 * existe repositorio para él: {@link co.edu.unbosque.service.AuditService} (escritura) y
 * {@link co.edu.unbosque.service.AuditQueryService} (lectura) usan {@code ElasticsearchOperations}
 * directamente.
 *
 * <p>No hay integridad referencial con las entidades relacionales: la correspondencia con el socio
 * es puramente semántica, a través de {@link #username}.
 *
 * <h2>Se expone al cliente sin frontera de DTO</h2>
 *
 * <p>{@code GET /audit} devuelve este documento tal cual, de modo que su forma es contrato público de
 * la API para el rol {@code ADMIN}.
 *
 * <h2>Advertencia sobre la fiabilidad de la bitácora</h2>
 *
 * <p>La escritura es {@code @Async} y captura toda excepción hacia {@code System.err}. Si
 * Elasticsearch no está disponible, la aplicación sigue funcionando con normalidad y <strong>los
 * eventos se descartan sin dejar constancia de la pérdida</strong>, incluidos los de severidad
 * {@code CRITICAL}. Una bitácora vacía no permite concluir que no ocurrió nada.
 *
 * @see AuditEventType
 * @see AuditResult
 * @see AuditSeverity
 */
@Document(indexName = "audit-log")
public class AuditEvent {

	/** Identificador del documento. Lo genera Elasticsearch; {@code AuditService} nunca lo asigna. */
	@Id
	private String id;

	/**
	 * Instante del evento.
	 *
	 * <p>Normalmente es el momento en que se registró, pero admite retrodatación: el evento
	 * {@code CHARGE_REGISTERED} usa la hora de apertura del consumo, no la de su registro.
	 */
	@Field(type = FieldType.Date, format = DateFormat.date_time)
	private Instant timestamp;

	/** Tipo de evento; uno de los valores de {@link AuditEventType}, sin verificación en compilación. */
	@Field(type = FieldType.Keyword)
	private String eventType;

	/** Desenlace del evento; uno de los valores de {@link AuditResult}. */
	@Field(type = FieldType.Keyword)
	private String result;

	/**
	 * Sujeto del evento: la {@code identification} del socio.
	 *
	 * <p><strong>Se almacena en claro.</strong> El enmascaramiento de
	 * {@link co.edu.unbosque.security.PiiMasking} se aplica únicamente al evento de solicitud de
	 * recuperación de contraseña, de modo que el resto del índice contiene cédulas sin enmascarar.
	 */
	@Field(type = FieldType.Keyword)
	private String username;

	/**
	 * Dirección IP atribuida al evento.
	 *
	 * <p>La resuelve {@link co.edu.unbosque.security.HttpRequestUtils#currentClientIp()}, que confía
	 * en la cabecera {@code X-Forwarded-For} <strong>sin validar el origen</strong>. Por tanto este
	 * valor es falsificable por el propio cliente y no debe tratarse como prueba.
	 */
	@Field(type = FieldType.Keyword)
	private String ipAddress;

	/**
	 * Descripción legible del evento.
	 *
	 * <p>Es el único campo de tipo {@code Text}, es decir analizado por Elasticsearch: sirve para
	 * búsqueda por texto, pero <strong>no admite comparación por igualdad exacta</strong> como los
	 * campos {@code Keyword}.
	 *
	 * <p>En el evento {@code ACCESS_DENIED} incorpora el URI solicitado sin sanear. Este backend no lo
	 * renderiza como HTML, pero cualquier consumidor que lo haga debe escaparlo.
	 */
	@Field(type = FieldType.Text)
	private String detail;

	/** Identificador del objeto afectado, cuando aplica. Para {@code CHARGE_REGISTERED}, el {@code consumptionId}. */
	@Field(type = FieldType.Keyword)
	private String targetId;

	/**
	 * Severidad; uno de los valores de {@link AuditSeverity}.
	 *
	 * <p>No la fija quien registra el evento: la deriva {@code AuditService} a partir del tipo y del
	 * desenlace, de modo que la clasificación es consistente en todo el sistema.
	 */
	@Field(type = FieldType.Keyword)
	private String severity;

	public AuditEvent() {
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Instant getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Instant timestamp) {
		this.timestamp = timestamp;
	}

	public String getEventType() {
		return eventType;
	}

	public void setEventType(String eventType) {
		this.eventType = eventType;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public String getDetail() {
		return detail;
	}

	public void setDetail(String detail) {
		this.detail = detail;
	}

	public String getTargetId() {
		return targetId;
	}

	public void setTargetId(String targetId) {
		this.targetId = targetId;
	}

	public String getSeverity() {
		return severity;
	}

	public void setSeverity(String severity) {
		this.severity = severity;
	}
}
