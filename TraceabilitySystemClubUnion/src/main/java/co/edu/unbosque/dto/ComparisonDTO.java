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

	/**
	 * Devuelve el total facturado del periodo actual.
	 *
	 * @return el total facturado del periodo actual
	 */
	public double getCurrentTotal() {
		return currentTotal;
	}

	/**
	 * Establece el total facturado del periodo actual.
	 *
	 * @param v el total facturado del periodo actual
	 */
	public void setCurrentTotal(double v) {
		this.currentTotal = v;
	}

	/**
	 * Devuelve el total facturado del periodo anterior.
	 *
	 * @return el total facturado del periodo anterior
	 */
	public double getPreviousTotal() {
		return previousTotal;
	}

	/**
	 * Establece el total facturado del periodo anterior.
	 *
	 * @param v el total facturado del periodo anterior
	 */
	public void setPreviousTotal(double v) {
		this.previousTotal = v;
	}

	/**
	 * Devuelve el número de cargos del periodo actual.
	 *
	 * @return el número de cargos del periodo actual
	 */
	public long getCurrentCount() {
		return currentCount;
	}

	/**
	 * Establece el número de cargos del periodo actual.
	 *
	 * @param v el número de cargos del periodo actual
	 */
	public void setCurrentCount(long v) {
		this.currentCount = v;
	}

	/**
	 * Devuelve el número de cargos del periodo anterior.
	 *
	 * @return el número de cargos del periodo anterior
	 */
	public long getPreviousCount() {
		return previousCount;
	}

	/**
	 * Establece el número de cargos del periodo anterior.
	 *
	 * @param v el número de cargos del periodo anterior
	 */
	public void setPreviousCount(long v) {
		this.previousCount = v;
	}

	/**
	 * Devuelve la variación porcentual respecto del periodo anterior.
	 *
	 * @return la variación porcentual respecto del periodo anterior
	 */
	public double getVariancePercentage() {
		return variancePercentage;
	}

	/**
	 * Establece la variación porcentual respecto del periodo anterior.
	 *
	 * @param v la variación porcentual respecto del periodo anterior
	 */
	public void setVariancePercentage(double v) {
		this.variancePercentage = v;
	}
}
