package co.edu.unbosque.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "monthly_snapshot")
public class MonthlySnapshot {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(unique = true)
	private String yearMonth;

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
