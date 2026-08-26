package co.edu.unbosque.dto;

import java.util.List;

public class PeakDTO {

	private List<HourBucketDTO> byHour;
	private List<WeekdayBucketDTO> byWeekday;

	public PeakDTO() {
	}

	public PeakDTO(List<HourBucketDTO> byHour, List<WeekdayBucketDTO> byWeekday) {
		this.byHour = byHour;
		this.byWeekday = byWeekday;
	}

	public List<HourBucketDTO> getByHour() {
		return byHour;
	}

	public void setByHour(List<HourBucketDTO> v) {
		this.byHour = v;
	}

	public List<WeekdayBucketDTO> getByWeekday() {
		return byWeekday;
	}

	public void setByWeekday(List<WeekdayBucketDTO> v) {
		this.byWeekday = v;
	}
}
