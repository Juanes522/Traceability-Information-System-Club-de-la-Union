package co.edu.unbosque.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import co.edu.unbosque.model.Access;

/**
 * Acceso a las visitas de los socios al club.
 *
 * <p>Convención transversal del repositorio: <strong>{@code dateTimeDeparture IS NULL} significa
 * «presente ahora»</strong>. Todas las consultas de presencia se apoyan en ese predicado.
 *
 * <p>Varios métodos de esta interfaz no tienen invocadores actualmente; se documentan igualmente y se
 * señala su situación, porque su existencia sugiere funcionalidad prevista que no se completo.
 */
@Repository
public interface AccessRepository extends JpaRepository<Access, Long> {

	/**
	 * Historial completo de visitas de un socio, de la más reciente a la más antigua.
	 *
	 * <p>Sin uso actualmente: la pantalla de «Historial Accesos» del frontend muestra en realidad el
	 * historial de inicios de sesión, tomado de la bitácora de auditoría, no estas filas.
	 *
	 * @param personId clave primaria del socio
	 * @return las visitas, ordenadas por entrada descendente
	 */
	List<Access> findByPartnerPersonIdOrderByDateTimeAdmissionDesc(Long personId);

	/**
	 * Busca la visita abierta de un socio, si la tiene.
	 *
	 * <p>Sostiene la invariante «como máximo una visita abierta por socio» que mantiene
	 * {@link co.edu.unbosque.service.AccessService}.
	 *
	 * <p><strong>Consideración importante:</strong> el tipo de retorno es {@code Optional}, pero la
	 * consulta <strong>puede coincidir legítimamente con más de una fila</strong> —de hecho lleva
	 * {@code ORDER BY}, lo que solo tiene sentido si se esperan varias—. La unicidad no está garantizada
	 * por ningún índice: depende de una comprobación previa a la inserción que no es segura frente a
	 * concurrencia. Si llegaran a existir dos visitas abiertas del mismo socio, este método lanzaría
	 * {@code IncorrectResultSizeDataAccessException} en cada llamada posterior, y como quien lo invoca
	 * captura y descarta las excepciones, el registro de presencia de ese socio se detendría en silencio.
	 *
	 * @param personId clave primaria del socio
	 * @return la visita sin salida registrada, si existe
	 */
	@Query("SELECT a FROM Access a WHERE a.partner.personId = :personId AND a.dateTimeDeparture IS NULL ORDER BY a.dateTimeAdmission DESC")
	Optional<Access> findOpenAccessByPartnerId(@Param("personId") Long personId);

	/**
	 * Indica si el socio se encuentra actualmente en el club.
	 *
	 * <p>Sin uso actualmente. Resuelve con un conteo la misma pregunta que
	 * {@link #findOpenAccessByPartnerId(Long)}, pero sin exponerse al problema de multiplicidad descrito
	 * allí.
	 *
	 * @param personId clave primaria del socio
	 * @return {@code true} si tiene alguna visita sin salida registrada
	 */
	@Query("SELECT COUNT(a) > 0 FROM Access a WHERE a.partner.personId = :personId AND a.dateTimeDeparture IS NULL")
	boolean isPartnerCurrentlyPresent(@Param("personId") Long personId);

	/**
	 * Visitas de un socio en un rango de fechas. Sin uso actualmente.
	 *
	 * @param personId clave primaria del socio
	 * @param from inicio del rango, inclusivo
	 * @param to fin del rango, inclusivo
	 * @return las visitas cuya entrada cae en el rango
	 */
	List<Access> findByPartnerPersonIdAndDateTimeAdmissionBetween(Long personId, LocalDateTime from, LocalDateTime to);

	/**
	 * Cuenta los socios presentes en el club en este momento.
	 *
	 * <p>Alimenta {@code AccessSummaryDTO.presentNow}. <strong>No acepta rango</strong>, y esa es la
	 * razón por la que ese indicador ignora la ventana temporal solicitada: en una consulta histórica
	 * devuelve un dato en vivo mezclado con datos del pasado.
	 *
	 * @return número de visitas sin salida registrada
	 */
	long countByDateTimeDepartureIsNull();

	/**
	 * Cuenta las visitas iniciadas en un rango.
	 *
	 * @param from inicio del rango, inclusivo
	 * @param to fin del rango, inclusivo
	 * @return número de visitas
	 */
	long countByDateTimeAdmissionBetween(LocalDateTime from, LocalDateTime to);

	/**
	 * Cuenta los socios <em>distintos</em> que visitaron el club en un rango.
	 *
	 * <p>Junto con {@link #countByDateTimeAdmissionBetween(LocalDateTime, LocalDateTime)} permite derivar
	 * la frecuencia media de visita por socio.
	 *
	 * @param from inicio del rango, inclusivo
	 * @param to fin del rango, inclusivo
	 * @return número de socios distintos
	 */
	@Query("SELECT COUNT(DISTINCT a.partner.personId) FROM Access a WHERE a.dateTimeAdmission BETWEEN :from AND :to")
	long countDistinctPartnersInRange(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

	/**
	 * Recupera todas las visitas de un rango, hidratando las entidades completas.
	 *
	 * <p>La usa {@code AccessMetricsService.attendance} para agrupar en memoria por día, semana o mes.
	 * <strong>No hay proyección ni tope</strong>: un rango amplio carga en memoria todas las visitas del
	 * periodo aunque solo se necesiten sus fechas de entrada.
	 *
	 * @param from inicio del rango, inclusivo
	 * @param to fin del rango, inclusivo
	 * @return las visitas del rango
	 */
	List<Access> findByDateTimeAdmissionBetween(LocalDateTime from, LocalDateTime to);

	/**
	 * Cuenta las visitas de un socio concreto en un rango. Alimenta {@code PartnerMetricsDTO.visits}.
	 *
	 * @param personId clave primaria del socio
	 * @param from inicio del rango, inclusivo
	 * @param to fin del rango, inclusivo
	 * @return número de visitas del socio
	 */
	long countByPartnerPersonIdAndDateTimeAdmissionBetween(Long personId, LocalDateTime from, LocalDateTime to);

	/**
	 * Última visita registrada de un socio.
	 *
	 * <p><strong>No acepta rango</strong>, y alimenta {@code PartnerMetricsDTO.lastVisit}. De ahí que ese
	 * campo pueda referirse a una fecha fuera de la ventana consultada, a diferencia del resto de campos
	 * del mismo DTO.
	 *
	 * @param personId clave primaria del socio
	 * @return la visita más reciente, si el socio tiene alguna
	 */
	Optional<Access> findFirstByPartnerPersonIdOrderByDateTimeAdmissionDesc(Long personId);

	/**
	 * Cierra en bloque <strong>todas</strong> las visitas abiertas del sistema, estampando la hora
	 * indicada como salida.
	 *
	 * <p>La invoca el trabajo programado {@code AccessService.closeOpenAccesses()} cada día a las 02:00.
	 * El {@code UPDATE} no discrimina por fecha de entrada, de modo que una visita abierta a las 23:00
	 * registra tres horas y una abierta a las 02:00:30 permanece abierta otras veinticuatro: la duración
	 * resultante es una aproximación con sesgo, no una medición.
	 *
	 * <p>Nótese que se declara {@code @Modifying} sin {@code clearAutomatically} ni
	 * {@code flushAutomatically}, así que las entidades ya cargadas en el contexto de persistencia no
	 * reflejan el cambio.
	 *
	 * @param now hora que se registrará como salida en todas las visitas abiertas
	 * @return número de filas modificadas
	 */
	@org.springframework.data.jpa.repository.Modifying
	@Query("UPDATE Access a SET a.dateTimeDeparture = :now WHERE a.dateTimeDeparture IS NULL")
	int closeOpenAccesses(@Param("now") LocalDateTime now);
}
