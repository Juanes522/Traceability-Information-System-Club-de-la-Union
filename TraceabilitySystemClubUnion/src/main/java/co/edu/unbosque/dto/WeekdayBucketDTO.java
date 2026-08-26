package co.edu.unbosque.dto;

public class WeekdayBucketDTO {

	private String weekday;
	private double total;
	private long count;

	public WeekdayBucketDTO() {
	}

	public WeekdayBucketDTO(String weekday, double total, long count) {
		this.weekday = weekday;
		this.total = total;
		this.count = count;
	}

	public String getWeekday() {
		return weekday;
	}

	public void setWeekday(String v) {
		this.weekday = v;
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
