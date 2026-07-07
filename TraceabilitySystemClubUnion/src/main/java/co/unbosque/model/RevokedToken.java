package co.unbosque.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "revoked_token")
public class RevokedToken {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(unique = true, nullable = false, length = 36)
	private String jti;

	@Column(name = "expiry_date", nullable = false)
	private LocalDateTime expiryDate;

	public RevokedToken() {
	}

	public RevokedToken(String jti, LocalDateTime expiryDate) {
		this.jti = jti;
		this.expiryDate = expiryDate;
	}

	public Long getId() {
		return id;
	}

	public String getJti() {
		return jti;
	}

	public void setJti(String jti) {
		this.jti = jti;
	}

	public LocalDateTime getExpiryDate() {
		return expiryDate;
	}

	public void setExpiryDate(LocalDateTime expiryDate) {
		this.expiryDate = expiryDate;
	}
}
