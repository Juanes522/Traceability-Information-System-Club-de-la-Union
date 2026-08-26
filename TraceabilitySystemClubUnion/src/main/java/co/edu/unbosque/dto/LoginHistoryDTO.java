package co.edu.unbosque.dto;

import java.time.Instant;

public class LoginHistoryDTO {
	private Instant timestamp;
	private String ip;

	public LoginHistoryDTO() {
	}

	public LoginHistoryDTO(Instant timestamp, String ip) {
		this.timestamp = timestamp;
		this.ip = ip;
	}

	public Instant getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Instant timestamp) {
		this.timestamp = timestamp;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}
}
