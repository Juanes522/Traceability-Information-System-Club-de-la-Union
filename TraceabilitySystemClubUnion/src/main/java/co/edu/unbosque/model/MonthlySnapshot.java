package co.edu.unbosque.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Agregados mensuales precalculados de facturación y afluencia.
 *
 * <p>Es una tabla de hechos derivados: cada fila resume un mes cerrado, para que las series
 * históricas no tengan que recalcularse agregando sobre todo el histórico de consumos.
 *
 * <p><strong>No tiene ninguna relación con otras entidades</strong>, y eso es intencional: sus
 * valores provienen de consultas de agregación, no de claves ajenas.
 *
 * <h2>Cómo se puebla</h2>
 *
 * <p>{@link co.edu.unbosque.service.SnapshotService} la escribe por dos vías: un trabajo programado
 * el día 1 de cada mes a las 03:00, y un relleno de meses faltantes que
 * {@link co.edu.unbosque.config.SnapshotBackfillRunner} ejecuta en cada arranque. La escritura es un
 * <em>upsert</em> por {@link #yearMonth}.
 *
 * <p>Como el relleno <strong>omite los meses que ya existen</strong>, una fila capturada a mitad de
 * mes —con datos incompletos— nunca se corrige después.
 *
 * <h2>Exposición parcial</h2>
 *
 * <p>{@code GET /metrics/snapshots} no devuelve todos los campos: {@code SnapshotService.list()}
 * descarta {@link #totalConsumption}, {@link #totalIva}, {@link #totalService}, {@link #totalTip} y
 * {@link #generatedAt}. Se persisten, pero ningún cliente los consulta.
 *
 * @see co.edu.unbosque.dto.MonthlySnapshotDTO
 */
@Entity
@Table(name = "monthly_snapshot")
public class MonthlySnapshot {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/**
	 * Mes resumido, en formato {@code YYYY-MM} (de {@code YearMonth.toString()}). Clave lógica de la
	 * tabla, con restricción de unicidad.
	 *
	 * <p>Ese formato ordena lexicográficamente igual que cronológicamente, razón por la que
	 * {@code findAllByOrderByYearMonthAsc()} devuelve la serie en orden temporal sin necesidad de
	 * conversión alguna.
	 *
	 * <p>Nótese que la columna resultante se llama {@code year_month}, que es palabra reservada en
	 * algunos motores aunque no lo sea en SQL Server.
	 */
	@Column(unique = true)
	private String yearMonth;

	/**
	 * Total facturado del mes: consumo neto más IVA, servicio y propina.
	 *
	 * <p>Todos los campos numéricos de esta entidad son <strong>primitivos</strong>, de modo que nunca
	 * son nulos: un mes sin actividad se almacena con ceros, no con nulos.
	 */
	private double totalBilled;
	private double totalConsumption;
	private double totalIva;
	private double totalService;
	private double totalTip;
	private long chargeCount;
	private double averagePerAccount;
	private double tipPercentage;
	private long visits;
	private long uniquePartners;
	private LocalDateTime generatedAt;

	/**
	 * Constructor sin argumentos requerido por el proveedor de persistencia.
	 *
	 * <p>No está pensado para usarse desde el código de la aplicación.
	 */
	public MonthlySnapshot() {
	}

	/**
	 * Devuelve el identificador.
	 *
	 * @return el identificador
	 */
	public Long getId() {
		return id;
	}

	/**
	 * Establece el identificador.
	 *
	 * @param id el identificador
	 */
	public void setId(Long id) {
		this.id = id;
	}

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

	/**
	 * Devuelve el momento en que se calculó el resumen.
	 *
	 * @return el momento en que se calculó el resumen
	 */
	public LocalDateTime getGeneratedAt() {
		return generatedAt;
	}

	/**
	 * Establece el momento en que se calculó el resumen.
	 *
	 * @param v el momento en que se calculó el resumen
	 */
	public void setGeneratedAt(LocalDateTime v) {
		this.generatedAt = v;
	}
}
