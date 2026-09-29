package co.edu.unbosque.dto;

/**
 * Indicadores globales de facturación de un periodo.
 *
 * <p>Contiene el desglose monetario, el total facturado, el número de cargos y dos valores derivados que conviene precisar:
 *
 * <ul>
 *   <li>El valor promedio se calcula por <strong>cargo</strong>, no por socio ni por visita.</li>
 *   <li>El porcentaje de propina se calcula sobre el <strong>consumo neto</strong> y no sobre el total facturado, que es la
 *       convención del negocio: la propina se expresa respecto a lo consumido, no a lo cobrado.</li>
 * </ul>
 *
 * <p>Los cuatro campos del desglose se calculan y se envían, pero <strong>ninguna pantalla los muestra</strong>: el tablero
 * presenta solo el total, el número de cargos, el promedio y el porcentaje de propina.
 */
public class ConsumptionSummaryDTO {

	private double totalBilled;
	private double totalConsumption;
	private double totalIva;
	private double totalService;
	private double totalTip;
	private long chargeCount;
	private double averagePerAccount;
	private double tipPercentage;

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
	 * Devuelve la suma del consumo neto.
	 *
	 * @return la suma del consumo neto
	 */
	public double getTotalConsumption() {
		return totalConsumption;
	}

	/**
	 * Establece la suma del consumo neto.
	 *
	 * @param v la suma del consumo neto
	 */
	public void setTotalConsumption(double v) {
		this.totalConsumption = v;
	}

	/**
	 * Devuelve la suma del impuesto al valor agregado.
	 *
	 * @return la suma del impuesto al valor agregado
	 */
	public double getTotalIva() {
		return totalIva;
	}

	/**
	 * Establece la suma del impuesto al valor agregado.
	 *
	 * @param v la suma del impuesto al valor agregado
	 */
	public void setTotalIva(double v) {
		this.totalIva = v;
	}

	/**
	 * Devuelve la suma de los recargos por servicio.
	 *
	 * @return la suma de los recargos por servicio
	 */
	public double getTotalService() {
		return totalService;
	}

	/**
	 * Establece la suma de los recargos por servicio.
	 *
	 * @param v la suma de los recargos por servicio
	 */
	public void setTotalService(double v) {
		this.totalService = v;
	}

	/**
	 * Devuelve la suma de las propinas.
	 *
	 * @return la suma de las propinas
	 */
	public double getTotalTip() {
		return totalTip;
	}

	/**
	 * Establece la suma de las propinas.
	 *
	 * @param v la suma de las propinas
	 */
	public void setTotalTip(double v) {
		this.totalTip = v;
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
}
