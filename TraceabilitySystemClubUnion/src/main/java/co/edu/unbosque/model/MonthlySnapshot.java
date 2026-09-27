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

	public MonthlySnapshot() {
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

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

	public LocalDateTime getGeneratedAt() {
		return generatedAt;
	}

	public void setGeneratedAt(LocalDateTime v) {
		this.generatedAt = v;
	}
}
