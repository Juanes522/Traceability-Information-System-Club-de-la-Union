package co.edu.unbosque.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import co.edu.unbosque.model.PushSubscription;

/**
 * Acceso a las suscripciones Web Push.
 *
 * <p>Es el único repositorio que un controlador inyecta y usa directamente:
 * {@link co.edu.unbosque.controller.PushSubscriptionController} persiste sin pasar por ningún servicio.
 */
@Repository
public interface PushSubscriptionRepository extends JpaRepository<PushSubscription, Long> {

	/**
	 * Todas las suscripciones de un socio, una por navegador o dispositivo.
	 *
	 * <p>La usa {@link co.edu.unbosque.service.PushNotificationService} para difundir el aviso de un cargo a
	 * todos los dispositivos del socio.
	 *
	 * <p><strong>Consulta a través de una columna cifrada:</strong> el recorrido llega a
	 * {@code partner.identification}, cifrada de forma determinista, y funciona porque el parámetro
	 * atraviesa el conversor antes de la comparación.
	 *
	 * <p>Como las suscripciones caducadas nunca se depuran, esta lista crece de forma monótona y cada
	 * notificación intenta la entrega contra todas sus filas.
	 *
	 * @param identification identificación del socio, en claro
	 * @return sus suscripciones; lista vacía si no tiene ninguna
	 */
	List<PushSubscription> findByPartnerIdentification(String identification);

	/**
	 * Busca una suscripción por su URL de entrega.
	 *
	 * <p>Permite que {@code POST /push/subscribe} sea idempotente: si el navegador vuelve a enviar un
	 * endpoint ya almacenado, el controlador responde 200 en lugar de crear un duplicado.
	 *
	 * @param endpoint URL del servicio push
	 * @return la suscripción, si ya está registrada
	 */
	Optional<PushSubscription> findByEndpoint(String endpoint);

	/**
	 * Elimina la suscripción correspondiente a una URL de entrega.
	 *
	 * <p><strong>Se declara sin {@code @Modifying} ni {@code @Transactional}</strong>, a diferencia de sus
	 * dos equivalentes en {@link PasswordResetTokenRepository#deleteByPartner(co.edu.unbosque.model.PersonPartner)}
	 * y {@link RevokedTokenRepository#deleteByExpiryDateBefore(java.time.LocalDateTime)}, que llevan ambas.
	 * Una consulta derivada de borrado necesita una transacción activa, de modo que
	 * {@code DELETE /push/unsubscribe} probablemente falle en ejecución en lugar de completar el borrado.
	 *
	 * <p>El defecto no se manifiesta porque ninguna pantalla invoca ese endpoint. Su consecuencia observable
	 * es otra: las suscripciones nunca se eliminan del servidor, ni por baja del usuario ni por caducidad
	 * del endpoint.
	 *
	 * @param endpoint URL del servicio push
	 */
	void deleteByEndpoint(String endpoint);
}
