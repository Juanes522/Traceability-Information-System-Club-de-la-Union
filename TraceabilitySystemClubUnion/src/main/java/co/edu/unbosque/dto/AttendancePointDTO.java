package co.edu.unbosque.dto;

public class AttendancePointDTO {

	private String bucket;
	private long count;

	public AttendancePointDTO() {
	}

	public AttendancePointDTO(String bucket, long count) {
		this.bucket = bucket;
		this.count = count;
	}

	public String getBucket() {
		return bucket;
	}

	public void setBucket(String v) {
		this.bucket = v;
	}

	public long getCount() {
		return count;
	}

	public void setCount(long v) {
		this.count = v;
	}
}
