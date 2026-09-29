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

	/**
	 * Constructor sin argumentos requerido por el proveedor de persistencia.
	 *
	 * <p>No está pensado para usarse desde el código de la aplicación.
	 */
	public AuditEvent() {
	}

	/**
	 * Devuelve el identificador.
	 *
	 * @return el identificador
	 */
	public String getId() {
		return id;
	}

	/**
	 * Establece el identificador.
	 *
	 * @param id el identificador
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * Devuelve el instante del evento.
	 *
	 * @return el instante del evento
	 */
	public Instant getTimestamp() {
		return timestamp;
	}

	/**
	 * Establece el instante del evento.
	 *
	 * @param timestamp el instante del evento
	 */
	public void setTimestamp(Instant timestamp) {
		this.timestamp = timestamp;
	}

	/**
	 * Devuelve el tipo de evento de auditoría.
	 *
	 * @return el tipo de evento de auditoría
	 */
	public String getEventType() {
		return eventType;
	}

	/**
	 * Establece el tipo de evento de auditoría.
	 *
	 * @param eventType el tipo de evento de auditoría
	 */
	public void setEventType(String eventType) {
		this.eventType = eventType;
	}

	/**
	 * Devuelve el desenlace del evento de auditoría.
	 *
	 * @return el desenlace del evento de auditoría
	 */
	public String getResult() {
		return result;
	}

	/**
	 * Establece el desenlace del evento de auditoría.
	 *
	 * @param result el desenlace del evento de auditoría
	 */
	public void setResult(String result) {
		this.result = result;
	}

	/**
	 * Devuelve el sujeto del evento, que es la cédula del socio sin enmascarar.
	 *
	 * @return el sujeto del evento, que es la cédula del socio sin enmascarar
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * Establece el sujeto del evento, que es la cédula del socio sin enmascarar.
	 *
	 * @param username el sujeto del evento, que es la cédula del socio sin enmascarar
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * Devuelve la dirección de origen atribuida al evento.
	 *
	 * @return la dirección de origen atribuida al evento
	 */
	public String getIpAddress() {
		return ipAddress;
	}

	/**
	 * Establece la dirección de origen atribuida al evento.
	 *
	 * @param ipAddress la dirección de origen atribuida al evento
	 */
	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	/**
	 * Devuelve la descripción legible del evento.
	 *
	 * @return la descripción legible del evento
	 */
	public String getDetail() {
		return detail;
	}

	/**
	 * Establece la descripción legible del evento.
	 *
	 * @param detail la descripción legible del evento
	 */
	public void setDetail(String detail) {
		this.detail = detail;
	}

	/**
	 * Devuelve el identificador del objeto afectado por el evento.
	 *
	 * @return el identificador del objeto afectado por el evento
	 */
	public String getTargetId() {
		return targetId;
	}

	/**
	 * Establece el identificador del objeto afectado por el evento.
	 *
	 * @param targetId el identificador del objeto afectado por el evento
	 */
	public void setTargetId(String targetId) {
		this.targetId = targetId;
	}

	/**
	 * Devuelve la severidad del evento, derivada de su tipo y desenlace.
	 *
	 * @return la severidad del evento, derivada de su tipo y desenlace
	 */
	public String getSeverity() {
		return severity;
	}

	/**
	 * Establece la severidad del evento, derivada de su tipo y desenlace.
	 *
	 * @param severity la severidad del evento, derivada de su tipo y desenlace
	 */
	public void setSeverity(String severity) {
		this.severity = severity;
	}
}
