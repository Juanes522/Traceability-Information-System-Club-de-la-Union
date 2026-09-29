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

	/**
	 * Constructor sin argumentos requerido para la deserialización del cuerpo de la petición.
	 */
	public ExternalSocioDTO() {
	}

	/**
	 * Devuelve la cédula del socio, que es el nombre de usuario del sistema.
	 *
	 * @return la cédula del socio, que es el nombre de usuario del sistema
	 */
	public String getIdentification() {
		return identification;
	}

	/**
	 * Establece la cédula del socio, que es el nombre de usuario del sistema.
	 *
	 * @param identification la cédula del socio, que es el nombre de usuario del sistema
	 */
	public void setIdentification(String identification) {
		this.identification = identification;
	}

	/**
	 * Devuelve el primer nombre.
	 *
	 * @return el primer nombre
	 */
	public String getFirstName() {
		return firstName;
	}

	/**
	 * Establece el primer nombre.
	 *
	 * @param firstName el primer nombre
	 */
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	/**
	 * Devuelve el segundo nombre.
	 *
	 * @return el segundo nombre
	 */
	public String getSecondName() {
		return secondName;
	}

	/**
	 * Establece el segundo nombre.
	 *
	 * @param secondName el segundo nombre
	 */
	public void setSecondName(String secondName) {
		this.secondName = secondName;
	}

	/**
	 * Devuelve el apellido.
	 *
	 * @return el apellido
	 */
	public String getLastName() {
		return lastName;
	}

	/**
	 * Establece el apellido.
	 *
	 * @param lastName el apellido
	 */
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	/**
	 * Devuelve el género.
	 *
	 * @return el género
	 */
	public String getGender() {
		return gender;
	}

	/**
	 * Establece el género.
	 *
	 * @param gender el género
	 */
	public void setGender(String gender) {
		this.gender = gender;
	}

	/**
	 * Devuelve las direcciones de correo del socio, cifradas en reposo.
	 *
	 * @return las direcciones de correo del socio, cifradas en reposo
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * Establece las direcciones de correo del socio, cifradas en reposo.
	 *
	 * @param email las direcciones de correo del socio, cifradas en reposo
	 */
	public void setEmail(String email) {
		this.email = email;
	}

	/**
	 * Devuelve el teléfono fijo, cifrado en reposo.
	 *
	 * @return el teléfono fijo, cifrado en reposo
	 */
	public String getPhone() {
		return phone;
	}

	/**
	 * Establece el teléfono fijo, cifrado en reposo.
	 *
	 * @param phone el teléfono fijo, cifrado en reposo
	 */
	public void setPhone(String phone) {
		this.phone = phone;
	}

	/**
	 * Devuelve el teléfono móvil, cifrado en reposo.
	 *
	 * @return el teléfono móvil, cifrado en reposo
	 */
	public String getCellPhone() {
		return cellPhone;
	}

	/**
	 * Establece el teléfono móvil, cifrado en reposo.
	 *
	 * @param cellPhone el teléfono móvil, cifrado en reposo
	 */
	public void setCellPhone(String cellPhone) {
		this.cellPhone = cellPhone;
	}

	/**
	 * Devuelve la fecha de nacimiento.
	 *
	 * @return la fecha de nacimiento
	 */
	public String getBirthDate() {
		return birthDate;
	}

	/**
	 * Establece la fecha de nacimiento.
	 *
	 * @param birthDate la fecha de nacimiento
	 */
	public void setBirthDate(String birthDate) {
		this.birthDate = birthDate;
	}

	/**
	 * Devuelve la fecha de ingreso al club.
	 *
	 * @return la fecha de ingreso al club
	 */
	public String getIngressDate() {
		return ingressDate;
	}

	/**
	 * Establece la fecha de ingreso al club.
	 *
	 * @param ingressDate la fecha de ingreso al club
	 */
	public void setIngressDate(String ingressDate) {
		this.ingressDate = ingressDate;
	}

	/**
	 * Devuelve el número de acción del club, que no es único.
	 *
	 * @return el número de acción del club, que no es único
	 */
	public Long getShareNumber() {
		return shareNumber;
	}

	/**
	 * Establece el número de acción del club, que no es único.
	 *
	 * @param shareNumber el número de acción del club, que no es único
	 */
	public void setShareNumber(Long shareNumber) {
		this.shareNumber = shareNumber;
	}
}
