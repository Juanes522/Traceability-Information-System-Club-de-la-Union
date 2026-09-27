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
 * Lista negra de JWT revocados, identificados por su {@code jti}.
 *
 * <p>Es la pieza que hace posible el cierre de sesión real en un esquema de tokens sin estado. Un JWT
 * firmado es válido hasta su expiración y no puede «apagarse»; esta tabla suple esa carencia
 * consultando, en cada petición, si el identificador del token figura revocado.
 *
 * <p>Es también el motivo por el que
 * {@link co.edu.unbosque.security.JwtUtil#generateToken(org.springframework.security.core.userdetails.UserDetails)}
 * incluye un {@code jti} aleatorio: sin él no habría nada que revocar.
 *
 * <h2>Costo y contrapartida</h2>
 *
 * <p>{@link co.edu.unbosque.security.JwtAuthenticationFilter} consulta esta tabla en <strong>cada
 * petición autenticada</strong>, sin caché. Sumado a la carga del usuario, son dos consultas por
 * petición. Es la razón por la que la cadena está declarada {@code STATELESS} pero el camino de la
 * petición es, de hecho, con estado.
 *
 * <p>{@link co.edu.unbosque.service.TokenBlacklistService#purgeExpired()} elimina cada hora las filas
 * ya vencidas, de modo que la tabla se mantiene acotada: un token expirado se rechaza por su propia
 * fecha y no necesita seguir en la lista.
 */
@Entity
@Table(name = "revoked_token")
public class RevokedToken {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/** Identificador único del token revocado (claim {@code jti}), un UUID de 36 caracteres. */
	@Column(unique = true, nullable = false, length = 36)
	private String jti;

	/**
	 * Expiración original del token.
	 *
	 * <p>Se conserva para poder depurar la fila cuando el token haya caducado por sí mismo: a partir de
	 * ese momento la revocación es redundante.
	 */
	@Column(name = "expiry_date", nullable = false)
	private LocalDateTime expiryDate;

	/**
	 * Socio propietario del token, cuando se pudo resolver.
	 *
	 * <p><strong>Admite nulo deliberadamente</strong>, a diferencia de las claves ajenas de
	 * {@code access}, {@code push_subscription} y {@code password_reset_token}: permite revocar un
	 * token cuyo sujeto ya no corresponde a ningún socio existente. La revocación nunca debe fallar por
	 * no poder identificar al propietario.
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "person_id")
	private PersonPartner person;

	public RevokedToken() {
	}

	public RevokedToken(String jti, LocalDateTime expiryDate) {
		this.jti = jti;
		this.expiryDate = expiryDate;
	}

	public RevokedToken(String jti, LocalDateTime expiryDate, PersonPartner person) {
		this.jti = jti;
		this.expiryDate = expiryDate;
		this.person = person;
	}

	public Long getId() {
		return id;
	}

	public String getJti() {
		return jti;
	}

	public void setJti(String jti) {
		this.jti = jti;
	}

	public LocalDateTime getExpiryDate() {
		return expiryDate;
	}

	public void setExpiryDate(LocalDateTime expiryDate) {
		this.expiryDate = expiryDate;
	}

	public PersonPartner getPerson() {
		return person;
	}

	public void setPerson(PersonPartner person) {
		this.person = person;
	}
}
