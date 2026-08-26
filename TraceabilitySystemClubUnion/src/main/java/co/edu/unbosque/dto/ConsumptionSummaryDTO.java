package co.edu.unbosque.dto;

public class ConsumptionSummaryDTO {

	private double totalBilled;
	private double totalConsumption;
	private double totalIva;
	private double totalService;
	private double totalTip;
	private long chargeCount;
	private double averagePerAccount;
	private double tipPercentage;

	public double getTotalBilled() {
		return totalBilled;
	}

	public void setTotalBilled(double v) {
		this.totalBilled = v;
	}

	public double getTotalConsumption() {
		return totalConsumption;
	}

	public void setTotalConsumption(double v) {
		this.totalConsumption = v;
	}

	public double getTotalIva() {
		return totalIva;
	}

	public void setTotalIva(double v) {
		this.totalIva = v;
	}

	public double getTotalService() {
		return totalService;
	}

	public void setTotalService(double v) {
		this.totalService = v;
	}

	public double getTotalTip() {
		return totalTip;
	}

	public void setTotalTip(double v) {
		this.totalTip = v;
	}

	public long getChargeCount() {
		return chargeCount;
	}

	public void setChargeCount(long v) {
		this.chargeCount = v;
	}

	public double getAveragePerAccount() {
		return averagePerAccount;
	}

	public void setAveragePerAccount(double v) {
		this.averagePerAccount = v;
	}

	public double getTipPercentage() {
		return tipPercentage;
	}

	public void setTipPercentage(double v) {
		this.tipPercentage = v;
	}
}
