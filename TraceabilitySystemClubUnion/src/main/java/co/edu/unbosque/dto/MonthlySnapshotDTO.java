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

	/**
	 * Devuelve el mes resumido, en formato de año y mes.
	 *
	 * @return el mes resumido, en formato de año y mes
	 */
	public String getYearMonth() {
		return yearMonth;
	}

	/**
	 * Establece el mes resumido, en formato de año y mes.
	 *
	 * @param v el mes resumido, en formato de año y mes
	 */
	public void setYearMonth(String v) {
		this.yearMonth = v;
	}

	/**
	 * Devuelve el total facturado, con impuestos y recargos incluidos.
	 *
	 * @return el total facturado, con impuestos y recargos incluidos
	 */
	public double getTotalBilled() {
		return totalBilled;
	}

	/**
	 * Establece el total facturado, con impuestos y recargos incluidos.
	 *
	 * @param v el total facturado, con impuestos y recargos incluidos
	 */
	public void setTotalBilled(double v) {
		this.totalBilled = v;
	}

	/**
	 * Devuelve el número de cargos del periodo.
	 *
	 * @return el número de cargos del periodo
	 */
	public long getChargeCount() {
		return chargeCount;
	}

	/**
	 * Establece el número de cargos del periodo.
	 *
	 * @param v el número de cargos del periodo
	 */
	public void setChargeCount(long v) {
		this.chargeCount = v;
	}

	/**
	 * Devuelve el valor promedio por cargo.
	 *
	 * @return el valor promedio por cargo
	 */
	public double getAveragePerAccount() {
		return averagePerAccount;
	}

	/**
	 * Establece el valor promedio por cargo.
	 *
	 * @param v el valor promedio por cargo
	 */
	public void setAveragePerAccount(double v) {
		this.averagePerAccount = v;
	}

	/**
	 * Devuelve el porcentaje de propina sobre el consumo neto.
	 *
	 * @return el porcentaje de propina sobre el consumo neto
	 */
	public double getTipPercentage() {
		return tipPercentage;
	}

	/**
	 * Establece el porcentaje de propina sobre el consumo neto.
	 *
	 * @param v el porcentaje de propina sobre el consumo neto
	 */
	public void setTipPercentage(double v) {
		this.tipPercentage = v;
	}

	/**
	 * Devuelve el número de visitas.
	 *
	 * @return el número de visitas
	 */
	public long getVisits() {
		return visits;
	}

	/**
	 * Establece el número de visitas.
	 *
	 * @param v el número de visitas
	 */
	public void setVisits(long v) {
		this.visits = v;
	}

	/**
	 * Devuelve el número de socios distintos del periodo.
	 *
	 * @return el número de socios distintos del periodo
	 */
	public long getUniquePartners() {
		return uniquePartners;
	}

	/**
	 * Establece el número de socios distintos del periodo.
	 *
	 * @param v el número de socios distintos del periodo
	 */
	public void setUniquePartners(long v) {
		this.uniquePartners = v;
	}
}
