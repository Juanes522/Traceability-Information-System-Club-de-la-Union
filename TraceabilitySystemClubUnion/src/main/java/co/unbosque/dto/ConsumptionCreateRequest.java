package co.unbosque.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public class ConsumptionCreateRequest {

	@NotNull
	private Long partnerId;
	@NotBlank
	@Size(max = 100)
	private String enviroment;
	private Integer account;
	@Size(max = 50)
	private String table;
	@NotBlank
	@Size(max = 100)
	private String waiterName;
	private Character isPartner;
	@NotNull
	@PositiveOrZero
	private Double consumptionValue;
	@NotNull
	@PositiveOrZero
	private Double iva;
	@NotNull
	@PositiveOrZero
	private Double service;
	@NotNull
	@PositiveOrZero
	private Double tip;
	@NotNull
	private LocalDate consumptionOpening;
	private LocalDate consumptionClosing;

	public ConsumptionCreateRequest() {
	}

	public Long getPartnerId() {
		return partnerId;
	}

	public void setPartnerId(Long partnerId) {
		this.partnerId = partnerId;
	}

	public String getEnviroment() {
		return enviroment;
	}

	public void setEnviroment(String enviroment) {
		this.enviroment = enviroment;
	}

	public Integer getAccount() {
		return account;
	}

	public void setAccount(Integer account) {
		this.account = account;
	}

	public String getTable() {
		return table;
	}

	public void setTable(String table) {
		this.table = table;
	}

	public String getWaiterName() {
		return waiterName;
	}

	public void setWaiterName(String waiterName) {
		this.waiterName = waiterName;
	}

	public Character getIsPartner() {
		return isPartner;
	}

	public void setIsPartner(Character isPartner) {
		this.isPartner = isPartner;
	}

	public Double getConsumptionValue() {
		return consumptionValue;
	}

	public void setConsumptionValue(Double consumptionValue) {
		this.consumptionValue = consumptionValue;
	}

	public Double getIva() {
		return iva;
	}

	public void setIva(Double iva) {
		this.iva = iva;
	}

	public Double getService() {
		return service;
	}

	public void setService(Double service) {
		this.service = service;
	}

	public Double getTip() {
		return tip;
	}

	public void setTip(Double tip) {
		this.tip = tip;
	}

	public LocalDate getConsumptionOpening() {
		return consumptionOpening;
	}

	public void setConsumptionOpening(LocalDate consumptionOpening) {
		this.consumptionOpening = consumptionOpening;
	}

	public LocalDate getConsumptionClosing() {
		return consumptionClosing;
	}

	public void setConsumptionClosing(LocalDate consumptionClosing) {
		this.consumptionClosing = consumptionClosing;
	}
}
