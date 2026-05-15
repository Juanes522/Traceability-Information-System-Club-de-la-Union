package co.unbosque.dto;

public class PushSubscriptionRequest {

	private String endpoint;
	private String p256dhKey;
	private String authKey;

	public PushSubscriptionRequest() {
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
}
