package co.edu.unbosque.dto;

public class AccessSummaryDTO {

	private long presentNow;
	private long visits;
	private long uniquePartners;
	private double avgFrequency;

	public long getPresentNow() {
		return presentNow;
	}

	public void setPresentNow(long v) {
		this.presentNow = v;
	}

	public long getVisits() {
		return visits;
	}

	public void setVisits(long v) {
		this.visits = v;
	}

	public long getUniquePartners() {
		return uniquePartners;
	}

	public void setUniquePartners(long v) {
		this.uniquePartners = v;
	}

	public double getAvgFrequency() {
		return avgFrequency;
	}

	public void setAvgFrequency(double v) {
		this.avgFrequency = v;
	}
}
