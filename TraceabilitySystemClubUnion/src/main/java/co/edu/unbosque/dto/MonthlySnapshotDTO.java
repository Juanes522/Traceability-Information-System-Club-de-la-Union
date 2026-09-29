package co.edu.unbosque.dto;

/**
 * Resumen mensual precalculado tal como se expone al cliente.
 *
 * <p>Es una proyección <strong>parcial</strong> de lo que se persiste: omite el desglose monetario y la fecha de generación,
 * que se calculan y se guardan pero ningún cliente consulta.
 *
 * <p>El mes viaja como cadena en formato de año y mes, un formato cuyo orden alfabético coincide con el cronológico, lo que
 * permite ordenar la serie sin convertir a fecha.
 */
public class MonthlySnapshotDTO {

	private String yearMonth;
	private double totalBilled;
	private long chargeCount;
	private double averagePerAccount;
	private double tipPercentage;
	private long visits;
	private long uniquePartners;

	public String getYearMonth() {
		return yearMonth;
	}

	public void setYearMonth(String v) {
		this.yearMonth = v;
	}

	public double getTotalBilled() {
		return totalBilled;
	}

	public void setTotalBilled(double v) {
		this.totalBilled = v;
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
}
