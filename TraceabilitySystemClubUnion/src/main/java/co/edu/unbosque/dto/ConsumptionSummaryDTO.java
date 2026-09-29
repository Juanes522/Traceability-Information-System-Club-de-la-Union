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
