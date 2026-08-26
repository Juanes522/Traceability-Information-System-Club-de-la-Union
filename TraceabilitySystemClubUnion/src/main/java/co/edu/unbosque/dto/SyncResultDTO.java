package co.edu.unbosque.dto;

public class SyncResultDTO {
	private long created;
	private long updated;

	public SyncResultDTO() {
	}

	public SyncResultDTO(long created, long updated) {
		this.created = created;
		this.updated = updated;
	}

	public long getCreated() {
		return created;
	}

	public void setCreated(long created) {
		this.created = created;
	}

	public long getUpdated() {
		return updated;
	}

	public void setUpdated(long updated) {
		this.updated = updated;
	}
}
