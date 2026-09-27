package co.edu.unbosque.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import co.edu.unbosque.model.Notification;

@Repository
/**
 * Acceso a los avisos generados por los consumos.
 *
 * <p>Particularidad del repositorio: {@link Notification} <strong>no tiene relación directa con el
 * socio</strong>, de modo que localizar los avisos de una persona exige un recorrido de tres niveles
 * ({@code notification → consumption → partner → identification}) que Spring Data expresa concatenando
 * los nombres de propiedad.
 */
public interface NotificationRepository extends JpaRepository<Notification, Long> {

	/**
	 * Avisos de un consumo concreto. Sin uso actualmente.
	 *
	 * @param consumptionId clave primaria del consumo
	 * @return los avisos asociados
	 */
	List<Notification> findByConsumptionConsumptionId(Long consumptionId);

	/**
	 * Todos los avisos de un socio, del más reciente al más antiguo.
	 *
	 * <p>Sin uso actualmente: solo lo alcanza un método de servicio que tampoco tiene invocadores. La
	 * variante paginada es la que está en producción.
	 *
	 * @param identification identificación del socio, en claro
	 * @return los avisos, ordenados por fecha de generación descendente
	 */
	List<Notification> findByConsumptionPartnerIdentificationOrderByGenerationDateDesc(String identification);

	/**
	 * Avisos de un socio, paginados y del más reciente al más antiguo.
	 *
	 * <p>Es la consulta que sirve {@code GET /personpartner/notifications/me}.
	 *
	 * <p><strong>Consulta a través de una columna cifrada.</strong> El recorrido termina en
	 * {@code partner.identification}, que está cifrada de forma determinista; el parámetro atraviesa el
	 * conversor antes de llegar al {@code WHERE}, de modo que la comparación es entre criptogramas. Con
	 * cifrado aleatorio esta consulta sería irrealizable.
	 *
	 * <p>Nótese que el orden está fijado en el nombre del método <em>y</em> se recibe un {@code Pageable}:
	 * quien lo construye no aporta {@code Sort}, así que prevalece el orden del nombre.
	 *
	 * <p>Consideración de rendimiento: quien consume el resultado desreferencia el consumo de cada aviso
	 * para recalcular su importe, lo que produce una consulta adicional por fila de la página.
	 *
	 * @param identification identificación del socio, en claro
	 * @param pageable       página solicitada
	 * @return página de avisos
	 */
	Page<Notification> findByConsumptionPartnerIdentificationOrderByGenerationDateDesc(
			String identification, Pageable pageable);
}
