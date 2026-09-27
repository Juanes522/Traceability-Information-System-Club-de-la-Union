package co.edu.unbosque.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import co.edu.unbosque.converter.DeterministicEncryptedStringConverter;
import co.edu.unbosque.converter.EncryptedStringArrayConverter;
import co.edu.unbosque.converter.EncryptedStringConverter;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

/**
 * Socio del club y, a la vez, usuario del sistema.
 *
 * <p>Esta entidad cumple dos papeles que en otros diseños estarían separados: describe al socio
 * (acción, datos de contacto, estado) y constituye la credencial de acceso (contraseña, rol,
 * consentimiento). <strong>No hay tabla de usuarios aparte</strong>, de modo que gerentes y
 * administradores son filas de {@code person_partner} distinguidas únicamente por su columna
 * {@code role}.
 *
 * <p>Es también la única entidad del sistema con campos cifrados en reposo.
 *
 * <h2>Papel en la autenticación</h2>
 *
 * <p>El nombre de usuario de Spring Security es {@link #identification}, no un correo ni un
 * identificador numérico. Esa decisión es la que obliga a que ese campo use cifrado determinista
 * en lugar de aleatorio, para que
 * {@link co.edu.unbosque.repository.PersonPartnerRepository#findByIdentification(String)} pueda
 * resolverlo comparando criptogramas.
 *
 * <h2>Advertencia: esta entidad se serializa tal cual como respuesta HTTP</h2>
 *
 * <p>No existe DTO de salida para {@code PersonPartner}. Los endpoints {@code /personpartner/me},
 * {@code /getall}, {@code /getallpaged} y las cuatro búsquedas la devuelven directamente, con dos
 * consecuencias que conviene conocer antes de modificar la clase:
 *
 * <ul>
 *   <li>{@link #password} <strong>no lleva {@code @JsonIgnore}</strong>, así que el hash BCrypt
 *       viaja al cliente.</li>
 *   <li>Los conversores descifran al leer, de modo que {@code identification}, {@code phone},
 *       {@code cellPhone} y {@code email} llegan al cliente <strong>en claro</strong>: el cifrado
 *       en reposo no protege la frontera HTTP.</li>
 * </ul>
 *
 * <p>Corolario para quien mantenga esta clase: <strong>cualquier campo que se añada aquí pasa a
 * ser API pública automáticamente</strong>, sin que medie ninguna decisión explícita.
 *
 * <h2>Borrado en cascada</h2>
 *
 * <p>{@link #accesses} y {@link #consumptions} se declaran con {@code cascade = CascadeType.ALL} y
 * {@code orphanRemoval = true}, así que eliminar un socio <strong>elimina todo su historial</strong>
 * de consumos, líneas de detalle, notificaciones y accesos. Es relevante tanto para el derecho de
 * supresión que promete la política de tratamiento de datos como para cualquier expectativa de
 * retención de la información contable.
 *
 * @see co.edu.unbosque.security.UserDetailsServiceImpl
 * @see co.edu.unbosque.converter.DeterministicEncryptedStringConverter
 */
@Entity
@Table(name = "person_partner")
public class PersonPartner {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long personId;

	/**
	 * Cédula o documento de identidad del socio, y <strong>nombre de usuario del sistema</strong>.
	 *
	 * <p>Cifrado de forma determinista ({@code DET:v1:}) precisamente para que siga siendo
	 * consultable por igualdad y para que la restricción {@code unique = true} —que la base aplica
	 * sobre el criptograma— funcione. Cambiarlo a
	 * {@link co.edu.unbosque.converter.EncryptedStringConverter} rompería en silencio tanto el
	 * inicio de sesión como la unicidad.
	 *
	 * <p>Sin {@code length} explícito, de modo que Hibernate le asigna 255 caracteres: holgado para
	 * el criptograma de una cédula, que ocupa alrededor de 59. Por eso el script de migración de
	 * bases preexistentes no necesita ensanchar esta columna, a diferencia de las otras tres
	 * cifradas.
	 */
	@Convert(converter = DeterministicEncryptedStringConverter.class)
	@Column(unique = true, nullable = false)
	private String identification;

	private String firstName;
	private String secondName;

	private String lastName;

	/**
	 * Contraseña, normalmente con hash BCrypt y prefijo {@code {bcrypt}}.
	 *
	 * <p>Puede contener también <strong>texto en claro heredado</strong>: el
	 * {@code DelegatingPasswordEncoder} de {@link co.edu.unbosque.security.SecurityConfig} compara
	 * los valores sin prefijo con {@code NoOpPasswordEncoder}, y
	 * {@link co.edu.unbosque.security.UserDetailsServiceImpl#updatePassword} los convierte a hash
	 * en el primer inicio de sesión exitoso.
	 *
	 * <p>Ese reencriptado automático provoca un guardado de la entidad completa, lo que a su vez
	 * cifra los datos de contacto todavía en claro. Es el motivo por el que el script de migración
	 * de anchos de columna es obligatorio en bases preexistentes.
	 *
	 * <p><strong>Este campo carece de {@code @JsonIgnore}</strong> y se serializa en las respuestas
	 * que devuelven la entidad.
	 */
	private String password;

	private LocalDate birthDate;
	private LocalDate ingressDate;

	/** Número de acción del club contra la que se cargan los consumos. <strong>No es único</strong>. */
	private Long shareNumber;
	private Boolean partnerState;

	/** Teléfono fijo. Cifrado con IV aleatorio ({@code ENC:v1:}), por tanto no consultable. */
	@Convert(converter = EncryptedStringConverter.class)
	@Column(length = 512)
	private String phone;

	/** Teléfono móvil. Cifrado con IV aleatorio ({@code ENC:v1:}), por tanto no consultable. */
	@Convert(converter = EncryptedStringConverter.class)
	@Column(length = 512)
	private String cellPhone;
	private Character gender;

	/**
	 * Direcciones de correo del socio.
	 *
	 * <p>{@link co.edu.unbosque.converter.EncryptedStringArrayConverter} une el arreglo con comas y
	 * cifra <strong>la cadena resultante como un único bloque</strong>, no elemento por elemento.
	 * De ahí tres consecuencias:
	 *
	 * <ul>
	 *   <li>No existe {@code findByEmail}: el criptograma es del CSV completo y usa IV aleatorio.
	 *       Buscar por correo obliga a recorrer la tabla descifrando, que es lo que hace
	 *       {@link co.edu.unbosque.service.PersonPartnerService#getByEmail(String)}.</li>
	 *   <li>La coma es separador y no hay escape, lo cual es admisible para direcciones de correo.</li>
	 *   <li>Los 1000 caracteres de la columna admiten unos 716 bytes de texto unido, del orden de
	 *       15 a 20 direcciones. <strong>Nada valida que quepan</strong>: el desbordamiento aparece
	 *       como error de truncamiento de SQL Server y llega al cliente como HTTP 500.</li>
	 * </ul>
	 *
	 * <p>Al leer nunca devuelve {@code null}, sino un arreglo vacío. Solo la primera dirección
	 * recibe las notificaciones de consumo.
	 */
	@Convert(converter = EncryptedStringArrayConverter.class)
	@Column(length = 1000)
	private String[] email;

	/**
	 * Indica si el socio debe cambiar su contraseña antes de poder usar la aplicación.
	 *
	 * <p>Inicializado a {@code true} en el propio campo, de modo que <strong>toda fila nueva nace
	 * obligando al cambio</strong>. Ese valor por defecto seguro es lo que hace tolerable que
	 * {@link co.edu.unbosque.service.PartnerSyncService} siembre la contraseña inicial de los socios
	 * importados con su propia cédula.
	 *
	 * <p>El frontend lo recibe en {@code AuthResponse.needsPasswordChange} y lo impone con un modal
	 * bloqueante en el {@code ShellComponent}.
	 */
	@Column(name = "force_password_change")
	private Boolean forcePasswordChange = true;

	/**
	 * Rol almacenado, en la forma que se le haya escrito.
	 *
	 * <p>El valor crudo no se usa nunca directamente: {@link #getRole()} lo normaliza. Véase la
	 * documentación de ese método, porque es el punto donde esta columna se convierte en autoridad
	 * de Spring Security.
	 */
	@Column(name = "role")
    private String role;

	/** Si el socio aceptó la política de tratamiento de datos personales. */
	private Boolean consentAccepted;
	/**
	 * Versión de la política que el socio aceptó.
	 *
	 * <p>Se compara con {@link co.edu.unbosque.config.ConsentPolicy#VERSION} para decidir si debe
	 * volver a aceptar: publicar una versión nueva obliga a todos los socios a consentir de nuevo.
	 */
	private String consentVersion;
	private java.time.LocalDateTime consentAcceptedAt;

	/**
	 * Visitas del socio al club, lado inverso de la asociación.
	 *
	 * <p>{@code @JsonIgnore} evita serializar el grafo completo en las respuestas que devuelven la
	 * entidad. El borrado en cascada con {@code orphanRemoval} implica que eliminar al socio borra
	 * su historial de accesos.
	 */
	@JsonIgnore
	@OneToMany(mappedBy = "partner", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private List<Access> accesses = new ArrayList<>();

	/**
	 * Consumos cargados al socio, lado inverso de la asociación.
	 *
	 * <p>Con {@code cascade = ALL} y {@code orphanRemoval = true}, eliminar al socio borra sus
	 * consumos y, por cascada transitiva, sus líneas de detalle y sus notificaciones.
	 *
	 * <p>Nótese que las consultas de consumos no pasan por esta colección: usan
	 * {@link co.edu.unbosque.repository.PartnerConsumptionRepository} con paginación, porque cargar
	 * el historial completo de un socio en memoria no es viable.
	 */
	@JsonIgnore
	@OneToMany(mappedBy = "partner", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private List<PartnerConsumption> consumptions = new ArrayList<>();

	public PersonPartner() {
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

	/**
	 * Devuelve el rol normalizado a la forma {@code ROLE_*} en mayúsculas.
	 *
	 * <p>Este método es <strong>la única fuente de las autoridades de Spring Security</strong>:
	 * {@link co.edu.unbosque.security.UserDetailsServiceImpl} construye con su resultado el
	 * {@code SimpleGrantedAuthority} del usuario. Es decir que las expresiones
	 * {@code @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")} de los controladores funcionan
	 * únicamente porque este método garantiza el prefijo.
	 *
	 * <p>Tolera cualquier grafía en la columna: la base puede contener {@code PARTNER},
	 * {@code manager} o {@code ROLE_ADMIN} indistintamente. {@link #setRole(String)} almacena
	 * literalmente lo que recibe, sin normalizar, de modo que la normalización ocurre solo en
	 * lectura.
	 *
	 * <p>Una columna nula o vacía produce {@code ROLE_PARTNER}, es decir <strong>el privilegio
	 * mínimo</strong>. Ese valor por defecto es deliberadamente seguro: una fila mal poblada
	 * degrada a socio en lugar de escalar a administrador.
	 *
	 * <p>Consecuencia para el cliente: el rol que aparece en el JSON y en
	 * {@link co.edu.unbosque.dto.AuthResponse} es siempre el normalizado, nunca el valor crudo de la
	 * columna.
	 *
	 * @return {@code ROLE_PARTNER}, {@code ROLE_MANAGER} o {@code ROLE_ADMIN} según el contenido de
	 *         la columna; {@code ROLE_PARTNER} si está vacía
	 */
	public String getRole() {
		if (role == null || role.trim().isEmpty()) {
			return "ROLE_PARTNER";
		}
		if (!role.toUpperCase().startsWith("ROLE_")) {
			return "ROLE_" + role.toUpperCase();
		}
		return role.toUpperCase();
	}

	/**
	 * Almacena el rol <strong>sin normalizar</strong>.
	 *
	 * <p>La normalización la aplica {@link #getRole()} en lectura, así que aquí se acepta cualquier
	 * grafía. {@link co.edu.unbosque.service.PartnerSyncService} se apoya en ello y escribe
	 * {@code "PARTNER"} sin prefijo.
	 *
	 * @param role rol en cualquier grafía; {@code null} o vacío se interpretará como
	 *             {@code ROLE_PARTNER} al leerse
	 */
	public void setRole(String role) {
		this.role = role;
	}

	public Boolean getConsentAccepted() {
		return consentAccepted;
	}

	public void setConsentAccepted(Boolean consentAccepted) {
		this.consentAccepted = consentAccepted;
	}

	public String getConsentVersion() {
		return consentVersion;
	}

	public void setConsentVersion(String consentVersion) {
		this.consentVersion = consentVersion;
	}

	public java.time.LocalDateTime getConsentAcceptedAt() {
		return consentAcceptedAt;
	}

	public void setConsentAcceptedAt(java.time.LocalDateTime consentAcceptedAt) {
		this.consentAcceptedAt = consentAcceptedAt;
	}

}
