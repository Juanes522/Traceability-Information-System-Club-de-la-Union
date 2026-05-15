package co.unbosque.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "push_subscription")
public class PushSubscription {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(length = 512, nullable = false, unique = true)
	private String endpoint;

	@Column(name = "p256dh_key", length = 256, nullable = false)
	private String p256dhKey;

	@Column(name = "auth_key", length = 64, nullable = false)
	private String authKey;

	@JsonIgnore
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "person_id", nullable = false)
	private PersonPartner partner;

	public PushSubscription() {
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getEndpoint() {
		return endpoint;
	}

	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}

	public String getP256dhKey() {
		return p256dhKey;
	}

	public void setP256dhKey(String p256dhKey) {
		this.p256dhKey = p256dhKey;
	}

	public String getAuthKey() {
		return authKey;
	}

	public void setAuthKey(String authKey) {
		this.authKey = authKey;
	}

	public PersonPartner getPartner() {
		return partner;
	}

	public void setPartner(PersonPartner partner) {
		this.partner = partner;
	}
}
