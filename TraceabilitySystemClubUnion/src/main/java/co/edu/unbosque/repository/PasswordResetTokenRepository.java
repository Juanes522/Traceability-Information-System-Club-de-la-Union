package co.edu.unbosque.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.model.PasswordResetToken;
import co.edu.unbosque.model.PersonPartner;

/**
 * Acceso a los tokens de recuperación de contraseña.
 */
@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

	/**
	 * Busca un token por su valor.
	 *
	 * <p>Es el punto de entrada de {@code POST /auth/reset-password}: el token recibido en el enlace del
	 * correo es la única credencial de esa operación. A diferencia de las consultas sobre socios, aquí no
	 * interviene ningún cifrado: el token se almacena en claro, y su seguridad reside en ser un UUID
	 * impredecible y de un solo uso.
	 *
	 * @param token valor del token
	 * @return el token, si existe. Que exista no implica que sea válido: la vigencia se comprueba aparte
	 *         con {@link PasswordResetToken#isExpired()}
	 */
	Optional<PasswordResetToken> findByToken(String token);

	/**
	 * Elimina todos los tokens de un socio.
	 *
	 * <p>Se invoca <strong>antes</strong> de emitir uno nuevo, de modo que solicitar la recuperación
	 * invalida cualquier enlace anterior. Es lo que garantiza que solo haya un token vigente por socio,
	 * ya que la tabla no tiene ninguna restricción que lo imponga.
	 *
	 * <p>Lleva {@code @Modifying} y {@code @Transactional} propios, igual que
	 * {@link RevokedTokenRepository#deleteByExpiryDateBefore(LocalDateTime)}, porque una consulta derivada
	 * de borrado necesita una transacción activa.
	 *
	 * @param partner socio cuyos tokens se eliminan
	 */
	@Modifying
	@Transactional
	void deleteByPartner(PersonPartner partner);
}
