package co.edu.unbosque.dto;

public class HourBucketDTO {

	private int hour;
	private double total;
	private long count;

	public HourBucketDTO() {
	}

	public HourBucketDTO(int hour, double total, long count) {
		this.hour = hour;
		this.total = total;
		this.count = count;
	}

	public int getHour() {
		return hour;
	}

	public void setHour(int v) {
		this.hour = v;
	}

	public double getTotal() {
		return total;
	}

	public void setTotal(double v) {
		this.total = v;
	}

	public long getCount() {
		return count;
	}

	public void setCount(long v) {
		this.count = v;
	}
}
