package co.unbosque.model;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name="consumption_validation")
public class ConsumptionValidation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long validationId;

    private Boolean presentPartner;
    private Boolean answerPartner;
    private LocalDate validationDate;

    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="consumption_id", unique = true)
    private PartnerConsumption consumption;

    public ConsumptionValidation(){}

    public void setConsumption(PartnerConsumption consumption){
        this.consumption = consumption;
    }

	public Long getValidationId() {
		return validationId;
	}

	public void setValidationId(Long validationId) {
		this.validationId = validationId;
	}

	public Boolean getPresentPartner() {
		return presentPartner;
	}

	public void setPresentPartner(Boolean presentPartner) {
		this.presentPartner = presentPartner;
	}

	public Boolean getAnswerPartner() {
		return answerPartner;
	}

	public void setAnswerPartner(Boolean answerPartner) {
		this.answerPartner = answerPartner;
	}

	public LocalDate getValidationDate() {
		return validationDate;
	}

	public void setValidationDate(LocalDate validationDate) {
		this.validationDate = validationDate;
	}

	public PartnerConsumption getConsumption() {
		return consumption;
	}

}
