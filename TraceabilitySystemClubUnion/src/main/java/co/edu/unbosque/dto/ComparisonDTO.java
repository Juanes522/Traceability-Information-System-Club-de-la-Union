package co.edu.unbosque.dto;

public class ComparisonDTO {

	private double currentTotal;
	private double previousTotal;
	private long currentCount;
	private long previousCount;
	private double variancePercentage;

	public double getCurrentTotal() {
		return currentTotal;
	}

	public void setCurrentTotal(double v) {
		this.currentTotal = v;
	}

	public double getPreviousTotal() {
		return previousTotal;
	}

	public void setPreviousTotal(double v) {
		this.previousTotal = v;
	}

	public long getCurrentCount() {
		return currentCount;
	}

	public void setCurrentCount(long v) {
		this.currentCount = v;
	}

	public long getPreviousCount() {
		return previousCount;
	}

	public void setPreviousCount(long v) {
		this.previousCount = v;
	}

	public double getVariancePercentage() {
		return variancePercentage;
	}

	public void setVariancePercentage(double v) {
		this.variancePercentage = v;
	}
}
