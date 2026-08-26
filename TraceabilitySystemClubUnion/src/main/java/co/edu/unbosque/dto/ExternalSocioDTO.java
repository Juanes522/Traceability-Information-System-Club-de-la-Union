package co.edu.unbosque.dto;

public class ExternalSocioDTO {

	private String identification;
	private String firstName;
	private String secondName;
	private String lastName;
	private String gender;
	private String email;
	private String phone;
	private String cellPhone;
	private String birthDate;
	private String ingressDate;
	private Long shareNumber;

	public ExternalSocioDTO() {
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

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
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

	public String getBirthDate() {
		return birthDate;
	}

	public void setBirthDate(String birthDate) {
		this.birthDate = birthDate;
	}

	public String getIngressDate() {
		return ingressDate;
	}

	public void setIngressDate(String ingressDate) {
		this.ingressDate = ingressDate;
	}

	public Long getShareNumber() {
		return shareNumber;
	}

	public void setShareNumber(Long shareNumber) {
		this.shareNumber = shareNumber;
	}
}
