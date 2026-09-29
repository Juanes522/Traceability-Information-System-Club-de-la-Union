package co.edu.unbosque.dto;

/**
 * Comparación de la facturación de un mes con la del mes anterior.
 *
 * <p>Cuando el mes previo no tuvo facturación, la variación se informa como cero en lugar de como un crecimiento infinito.
 *
 * <p>Conviene saber que el cálculo que lo produce <strong>no corrige la frontera del mes</strong>, de modo que un consumo
 * registrado exactamente a medianoche del día primero se cuenta en ambos periodos. El resumen mensual resuelve esa misma
 * frontera correctamente, así que las dos cifras pueden discrepar.
 */
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
