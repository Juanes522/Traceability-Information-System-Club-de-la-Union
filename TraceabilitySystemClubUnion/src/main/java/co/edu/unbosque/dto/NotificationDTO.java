package co.edu.unbosque.dto;

import java.time.LocalDateTime;

public class NotificationDTO {

	private Long notificationId;
	private String title;
	private String body;
	private LocalDateTime generationDate;
	private Character state;
	private Long consumptionId;
	private String environment;
	private Double totalAmount;

	public NotificationDTO() {
	}

	public NotificationDTO(Long notificationId, String title, String body, LocalDateTime generationDate,
			Character state, Long consumptionId, String environment, Double totalAmount) {
		this.notificationId = notificationId;
		this.title = title;
		this.body = body;
		this.generationDate = generationDate;
		this.state = state;
		this.consumptionId = consumptionId;
		this.environment = environment;
		this.totalAmount = totalAmount;
	}

	public Long getNotificationId() {
		return notificationId;
	}

	public void setNotificationId(Long notificationId) {
		this.notificationId = notificationId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public LocalDateTime getGenerationDate() {
		return generationDate;
	}

	public void setGenerationDate(LocalDateTime generationDate) {
		this.generationDate = generationDate;
	}

	public Character getState() {
		return state;
	}

	public void setState(Character state) {
		this.state = state;
	}

	public Long getConsumptionId() {
		return consumptionId;
	}

	public void setConsumptionId(Long consumptionId) {
		this.consumptionId = consumptionId;
	}

	public String getEnvironment() {
		return environment;
	}

	public void setEnvironment(String environment) {
		this.environment = environment;
	}

	public Double getTotalAmount() {
		return totalAmount;
	}

	public void setTotalAmount(Double totalAmount) {
		this.totalAmount = totalAmount;
	}
}
