package co.unbosque.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name="partner_consumption")
public class PartnerConsumption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long consumptionId;

    private String enviroment;
    private Integer account;

    @Column(name="table_number")
    private String table;

    private String waiterName;
    private Character isPartner;
    private Character stateAccount;

    private Double consumptionValue;
    private Double iva;
    private Double service;
    private Double tip;

    private LocalDate consumptionOpening;
    private LocalDate consumptionClosing;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="person_id")
    private PersonPartner partner;

    @JsonIgnore
    @OneToMany(
        mappedBy = "consumption",
        cascade = CascadeType.ALL,
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    private List<Notification> notifications = new ArrayList<>();

    @JsonIgnore
    @OneToOne(
        mappedBy = "consumption",
        cascade = CascadeType.ALL,
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    private ConsumptionValidation validation;

    public PartnerConsumption() {}

    public void addNotification(Notification n){
        notifications.add(n);
        n.setConsumption(this);
    }

    public void removeNotification(Notification n){
        notifications.remove(n);
        n.setConsumption(null);
    }

    public void setValidation(ConsumptionValidation validation){
        this.validation = validation;
        if(validation != null){
            validation.setConsumption(this);
        }
    }

	public Long getConsumptionId() {
		return consumptionId;
	}

	public void setConsumptionId(Long consumptionId) {
		this.consumptionId = consumptionId;
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

	public Character getStateAccount() {
		return stateAccount;
	}

	public void setStateAccount(Character stateAccount) {
		this.stateAccount = stateAccount;
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

	public PersonPartner getPartner() {
		return partner;
	}

	public void setPartner(PersonPartner partner) {
		this.partner = partner;
	}

	public List<Notification> getNotifications() {
		return notifications;
	}

	public void setNotifications(List<Notification> notifications) {
		this.notifications = notifications;
	}

	public ConsumptionValidation getValidation() {
		return validation;
	}
	
}