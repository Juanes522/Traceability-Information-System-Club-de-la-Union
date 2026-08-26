package co.edu.unbosque.dto;

public class UserFailedCountDTO {

	private String username;
	private long count;

	public UserFailedCountDTO() {
	}

	public UserFailedCountDTO(String username, long count) {
		this.username = username;
		this.count = count;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String v) {
		this.username = v;
	}

	public long getCount() {
		return count;
	}

	public void setCount(long v) {
		this.count = v;
	}
}
