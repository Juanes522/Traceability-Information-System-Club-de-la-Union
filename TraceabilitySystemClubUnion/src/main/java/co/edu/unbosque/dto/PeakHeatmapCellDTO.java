package co.edu.unbosque.dto;

public class PeakHeatmapCellDTO {

	private int weekday;
	private int hour;
	private double total;
	private long count;

	public PeakHeatmapCellDTO() {
	}

	public PeakHeatmapCellDTO(int weekday, int hour, double total, long count) {
		this.weekday = weekday;
		this.hour = hour;
		this.total = total;
		this.count = count;
	}

	public int getWeekday() {
		return weekday;
	}

	public void setWeekday(int v) {
		this.weekday = v;
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
