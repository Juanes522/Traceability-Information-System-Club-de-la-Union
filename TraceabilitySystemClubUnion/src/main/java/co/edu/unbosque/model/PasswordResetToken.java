package co.edu.unbosque.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Token de un solo uso para restablecer la contraseña de un socio.
 *
 * <p>Ciclo de vida completo, gobernado por {@link co.edu.unbosque.controller.AuthController}:
 *
 * <ol>
 *   <li>{@code POST /auth/forgot-password} <strong>elimina los tokens previos del socio</strong> y
 *       crea uno nuevo con vigencia de una hora.</li>
 *   <li>El token viaja en el enlace del correo que envía
 *       {@link co.edu.unbosque.service.EmailService}.</li>
 *   <li>{@code POST /auth/reset-password} lo valida y, tras fijar la contraseña,
 *       <strong>lo elimina</strong>: no es reutilizable.</li>
 * </ol>
 *
 * <p>Que solo pueda existir un token vigente por socio es consecuencia del borrado previo, no de una
 * restricción de la tabla.
 */
@Entity
@Table(name = "password_reset_token")
public class PasswordResetToken {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/**
	 * Valor del token: un UUID generado en el servidor, de ahí la longitud 36.
	 *
	 * <p>Ser impredecible es su única protección: quien lo posea puede fijar la contraseña del socio
	 * sin conocer la anterior.
	 */
	@Column(unique = true, nullable = false, length = 36)
	private String token;

	/**
	 * Momento de caducidad.
	 *
	 * <p>La vigencia de una hora la calcula {@code AuthController} con {@code plusHours(1)}. Ese plazo
	 * está <strong>duplicado</strong>: {@code EmailService} incrusta además el texto «Este enlace es
	 * válido por 1 hora» en el cuerpo del correo, de modo que cambiar uno sin el otro produce un
	 * mensaje que miente al usuario.
	 */
	@Column(name = "expiry_date", nullable = false)
	private LocalDateTime expiryDate;

	/** Socio al que pertenece el token. No admite nulo. */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "person_id", nullable = false)
	private PersonPartner partner;

	public PasswordResetToken() {
	}

	public PasswordResetToken(String token, PersonPartner partner, LocalDateTime expiryDate) {
		this.token = token;
		this.partner = partner;
		this.expiryDate = expiryDate;
	}

	/**
	 * Indica si el token ya caducó.
	 *
	 * <p>Es el único predicado de dominio del paquete {@code model}: el resto de las entidades son
	 * contenedores de datos sin comportamiento. La caducidad se comprueba en la lectura, no con un
	 * trabajo de limpieza: los tokens vencidos permanecen en la tabla hasta que el socio solicita otro
	 * (lo que borra los anteriores).
	 *
	 * @return {@code true} si el instante actual es posterior a la fecha de caducidad
	 */
	public boolean isExpired() {
		return LocalDateTime.now().isAfter(this.expiryDate);
	}

	public Long getId() {
		return id;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public LocalDateTime getExpiryDate() {
		return expiryDate;
	}

	public void setExpiryDate(LocalDateTime expiryDate) {
		this.expiryDate = expiryDate;
	}

	public PersonPartner getPartner() {
		return partner;
	}

	public void setPartner(PersonPartner partner) {
		this.partner = partner;
	}
}
