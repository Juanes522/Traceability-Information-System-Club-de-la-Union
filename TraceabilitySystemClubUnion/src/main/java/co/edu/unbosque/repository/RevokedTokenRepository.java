package co.edu.unbosque.repository;

import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.model.RevokedToken;

/**
 * Acceso a la lista negra de JWT revocados.
 */
@Repository
public interface RevokedTokenRepository extends JpaRepository<RevokedToken, Long> {

	/**
	 * Indica si un token fue revocado.
	 *
	 * <p><strong>Es la consulta más frecuente de todo el sistema:</strong>
	 * {@link co.edu.unbosque.security.JwtAuthenticationFilter} la ejecuta en cada petición autenticada,
	 * sin caché alguna. Junto con la carga del usuario, son dos consultas a la base por petición. Es el
	 * costo de poder revocar tokens en un esquema declarado sin estado.
	 *
	 * <p>La usa también {@code TokenBlacklistService.revoke} para no insertar duplicados, comprobación
	 * previa a la inserción que no es segura frente a concurrencia y que, en caso de carrera, quedaría
	 * resuelta por la restricción de unicidad de la columna.
	 *
	 * @param jti identificador del token (claim {@code jti})
	 * @return {@code true} si el token está revocado
	 */
	boolean existsByJti(String jti);

	/**
	 * Elimina las revocaciones de tokens que ya caducaron por sí mismos.
	 *
	 * <p>La invoca el trabajo programado {@code TokenBlacklistService.purgeExpired()} cada hora. Mantener
	 * un token expirado en la lista es innecesario: se rechaza igualmente por su propia fecha de
	 * expiración. Gracias a esta depuración la tabla se mantiene acotada y la consulta de
	 * {@link #existsByJti(String)} no se degrada con el tiempo.
	 *
	 * @param cutoff instante límite; se eliminan las filas cuya expiración sea anterior
	 */
	@Modifying
	@Transactional
	void deleteByExpiryDateBefore(LocalDateTime cutoff);
}
