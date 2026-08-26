package co.edu.unbosque.model;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Document(indexName = "audit-log")
public class AuditEvent {

	@Id
	private String id;

	@Field(type = FieldType.Date, format = DateFormat.date_time)
	private Instant timestamp;

	@Field(type = FieldType.Keyword)
	private String eventType;

	@Field(type = FieldType.Keyword)
	private String result;

	@Field(type = FieldType.Keyword)
	private String username;

	@Field(type = FieldType.Keyword)
	private String ipAddress;

	@Field(type = FieldType.Text)
	private String detail;

	@Field(type = FieldType.Keyword)
	private String targetId;

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
