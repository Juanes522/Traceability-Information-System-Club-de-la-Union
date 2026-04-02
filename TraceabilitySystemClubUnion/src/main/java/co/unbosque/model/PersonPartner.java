package co.unbosque.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import co.unbosque.converter.StringArrayConverter;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "person_partner")
public class PersonPartner {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long personId;

	@Column(unique = true, nullable = false)
	private String identification;

	private String firstName;
	private String secondName;

	private String lastName;

	private String password;

	@JsonIgnore
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "owner_id")
	private PersonPartner owner;

	@JsonIgnore
	@OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private List<PersonPartner> dependents = new ArrayList<>();

	private LocalDate birthDate;
	private LocalDate ingressDate;

	private Long shareNumber;
	private Integer sequence;

	private String kinship;
	private String partnerKind;
	private Boolean partnerState;

	private String phone;
	private String cellPhone;
	private Character gender;

	@Convert(converter = StringArrayConverter.class)
	private String[] email;

	@Column(name = "force_password_change")
	private Boolean forcePasswordChange = true;
	
	@Column(name = "role")
    private String role;

	@JsonIgnore
	@OneToMany(mappedBy = "partner", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private List<Access> accesses = new ArrayList<>();

	@JsonIgnore
	@OneToMany(mappedBy = "partner", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private List<PartnerConsumption> consumptions = new ArrayList<>();

	public PersonPartner() {
	}

	public void addDependent(PersonPartner dependent) {
		dependents.add(dependent);
		dependent.setOwner(this);
	}

	public void removeDependent(PersonPartner dependent) {
		dependents.remove(dependent);
		dependent.setOwner(null);
	}

	public Long getPersonId() {
		return personId;
	}

	public void setPersonId(Long personId) {
		this.personId = personId;
	}

	public String getIdentification() {
		return identification;
	}

	public void setIdentification(String identification) {
		this.identification = identification;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getSecondName() {
		return secondName;
	}

	public void setSecondName(String secondName) {
		this.secondName = secondName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public PersonPartner getOwner() {
		return owner;
	}

	public void setOwner(PersonPartner owner) {
		this.owner = owner;
	}

	public List<PersonPartner> getDependents() {
		return dependents;
	}

	public void setDependents(List<PersonPartner> dependents) {
		this.dependents = dependents;
	}

	public LocalDate getBirthDate() {
		return birthDate;
	}

	public void setBirthDate(LocalDate birthDate) {
		this.birthDate = birthDate;
	}

	public LocalDate getIngressDate() {
		return ingressDate;
	}

	public void setIngressDate(LocalDate ingressDate) {
		this.ingressDate = ingressDate;
	}

	public Long getShareNumber() {
		return shareNumber;
	}

	public void setShareNumber(Long shareNumber) {
		this.shareNumber = shareNumber;
	}

	public Integer getSequence() {
		return sequence;
	}

	public void setSequence(Integer sequence) {
		this.sequence = sequence;
	}

	public String getKinship() {
		return kinship;
	}

	public void setKinship(String kinship) {
		this.kinship = kinship;
	}

	public String getPartnerKind() {
		return partnerKind;
	}

	public void setPartnerKind(String partnerKind) {
		this.partnerKind = partnerKind;
	}

	public Boolean getPartnerState() {
		return partnerState;
	}

	public void setPartnerState(Boolean partnerState) {
		this.partnerState = partnerState;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getCellPhone() {
		return cellPhone;
	}

	public void setCellPhone(String cellPhone) {
		this.cellPhone = cellPhone;
	}

	public Character getGender() {
		return gender;
	}

	public void setGender(Character gender) {
		this.gender = gender;
	}

	public String[] getEmail() {
		return email;
	}

	public void setEmail(String[] email) {
		this.email = email;
	}

	public Boolean getForcePasswordChange() {
		return forcePasswordChange;
	}

	public void setForcePasswordChange(Boolean forcePasswordChange) {
		this.forcePasswordChange = forcePasswordChange;
	}

	public List<Access> getAccesses() {
		return accesses;
	}

	public void setAccesses(List<Access> accesses) {
		this.accesses = accesses;
	}

	public List<PartnerConsumption> getConsumptions() {
		return consumptions;
	}

	public void setConsumptions(List<PartnerConsumption> consumptions) {
		this.consumptions = consumptions;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}
	
}
