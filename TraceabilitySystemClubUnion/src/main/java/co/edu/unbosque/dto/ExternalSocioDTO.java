package co.edu.unbosque.dto;

/**
 * Registro de socio tal como lo entrega el maestro externo del club.
 *
 * <p>Es la frontera de entrada de la única integración saliente del sistema, y su tipado es deliberadamente laxo porque un
 * feed externo no es confiable: el género y las dos fechas llegan como texto y se convierten después.
 *
 * <p>Pérdidas de información conocidas al mapearlo a la entidad:
 *
 * <ul>
 *   <li>Trae <strong>un solo correo</strong>, que reemplaza el arreglo completo de la entidad y descarta las direcciones
 *       adicionales que el socio tuviera.</li>
 *   <li>El género se reduce a su primer carácter.</li>
 *   <li>Una fecha malformada se convierte en nulo en silencio.</li>
 * </ul>
 *
 * <p><strong>No tiene ninguna anotación de validación.</strong> Un registro sin identificación se intentará persistir y
 * fallará contra la restricción de la base, sin error controlado.
 *
 * <p>El feed no conoce ni la contraseña, ni el rol, ni el estado, ni el consentimiento: esos datos son propiedad del
 * backend y la sincronización no los toca.
 */
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
