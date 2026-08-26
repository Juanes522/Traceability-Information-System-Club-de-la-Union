package co.edu.unbosque.model;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "access")
public class Access {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long accessId;

	private LocalDateTime dateTimeAdmission;
	private LocalDateTime dateTimeDeparture;

	@JsonIgnore
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "person_id", nullable = false)
	private PersonPartner partner;

	public Access() {
	}

	public Long getAccessId() {
		return accessId;
	}

	public void setAccessId(Long accessId) {
		this.accessId = accessId;
	}

	public LocalDateTime getDateTimeAdmission() {
		return dateTimeAdmission;
	}

	public void setDateTimeAdmission(LocalDateTime dateTimeAdmission) {
		this.dateTimeAdmission = dateTimeAdmission;
	}

	public LocalDateTime getDateTimeDeparture() {
		return dateTimeDeparture;
	}

	public void setDateTimeDeparture(LocalDateTime dateTimeDeparture) {
		this.dateTimeDeparture = dateTimeDeparture;
	}

	public PersonPartner getPartner() {
		return partner;
	}

	public void setPartner(PersonPartner partner) {
		this.partner = partner;
	}
}
