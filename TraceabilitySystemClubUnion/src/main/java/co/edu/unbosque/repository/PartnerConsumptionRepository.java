package co.edu.unbosque.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import co.edu.unbosque.dto.ConsumptionRowView;
import co.edu.unbosque.model.PartnerConsumption;

/**
 * Acceso a los consumos: es el repositorio más extenso del sistema y el que sostiene toda la analítica
 * de facturación.
 *
 * <p>Sus métodos se agrupan en tres familias con propósitos claramente distintos:
 *
 * <ol>
 *   <li><strong>Consultas derivadas paginadas</strong> — sirven los listados de la interfaz, filtrando
 *       por socio o por ambiente, con o sin rango de fechas. Cada endpoint elige la variante según los
 *       parámetros que reciba.</li>
 *   <li><strong>Consultas derivadas sin paginar</strong> — las consumen los reportes y las métricas de
 *       socio, que necesitan todo el conjunto; ninguna impone tope.</li>
 *   <li><strong>Agregaciones con {@code @Query}</strong> — calculan en la base y devuelven
 *       {@code List<Object[]>} posicional o una proyección, evitando traer entidades.</li>
 * </ol>
 *
 * <p>Nótese que el nombre de propiedad para el ambiente es {@code enviroment}, sin la segunda «n»,
 * porque así se llama el campo de la entidad: las consultas derivadas no compilarían con la grafía
 * correcta.
 */
public interface PartnerConsumptionRepository extends JpaRepository<PartnerConsumption, Long> {

	/**
	 * Busca un consumo por su clave primaria. Equivale a {@code findById} y no tiene invocadores.
	 *
	 * @param consumptionId clave primaria
	 * @return el consumo, si existe
	 */
	public Optional<PartnerConsumption> findByConsumptionId(Long consumptionId);

	/**
	 * Todos los consumos de un socio, sin paginar.
	 *
	 * <p>Sirve {@code GET /partnerconsumption/by-partner/{partnerId}}, el único endpoint de consumos que no
	 * pagina. El frontend no lo usa.
	 *
	 * @param personId clave primaria del socio
	 * @return los consumos del socio
	 */
	public List<PartnerConsumption> findByPartnerPersonId(Long personId);

	/**
	 * Consumos de un socio, paginados y sin filtro de fechas.
	 *
	 * <p>Es la variante que se aplica cuando el cliente no acota la ventana temporal.
	 *
	 * @param personId clave primaria del socio
	 * @param pageable página solicitada, normalmente ordenada por apertura descendente
	 * @return página de consumos
	 */
	Page<PartnerConsumption> findByPartnerPersonId(Long personId, Pageable pageable);

	/**
	 * Consumos de un socio dentro de un rango, paginados.
	 *
	 * <p>Sirve {@code /personpartner/getconsumptions/me} y su equivalente por identificación cuando el
	 * cliente aporta ambos extremos del rango. Esos endpoints limitan la ventana a 92 días.
	 *
	 * @param personId clave primaria del socio
	 * @param from inicio del rango, inclusivo
	 * @param to fin del rango, inclusivo
	 * @param pageable página solicitada
	 * @return página de consumos
	 */
	Page<PartnerConsumption> findByPartnerPersonIdAndConsumptionOpeningBetween(
			Long personId, LocalDateTime from, LocalDateTime to, Pageable pageable);

	/**
	 * Todos los consumos de un ambiente, sin paginar.
	 *
	 * <p>Alcanzable solo desde un método de servicio que a su vez no tiene invocadores.
	 *
	 * @param enviroment nombre del ambiente, con coincidencia exacta
	 * @return los consumos de ese ambiente
	 */
	public List<PartnerConsumption> findByEnviroment(String enviroment);

	/**
	 * Consumos de un ambiente, paginados y sin filtro de fechas.
	 *
	 * @param enviroment nombre del ambiente, con coincidencia exacta
	 * @param pageable página solicitada
	 * @return página de consumos
	 */
	Page<PartnerConsumption> findByEnviroment(String enviroment, Pageable pageable);

	/**
	 * Consumos de un ambiente dentro de un rango, paginados.
	 *
	 * <p>Sirve {@code GET /partnerconsumption/by-environment/{env}}. La coincidencia del ambiente es
	 * exacta y no hay catálogo que la respalde, de modo que una errata en el nombre devuelve una página
	 * vacía sin ninguna indicación de por qué.
	 *
	 * @param enviroment nombre del ambiente, con coincidencia exacta
	 * @param from inicio del rango, inclusivo
	 * @param to fin del rango, inclusivo
	 * @param pageable página solicitada
	 * @return página de consumos
	 */
	Page<PartnerConsumption> findByEnviromentAndConsumptionOpeningBetween(
			String enviroment, LocalDateTime from, LocalDateTime to, Pageable pageable);

	/**
	 * Todos los consumos de un rango, con las entidades completas.
	 *
	 * <p>La usa {@link co.edu.unbosque.service.ReportService} para el reporte de consumos.
	 * <strong>Sin paginación ni tope</strong>: carga en memoria todos los consumos del periodo —que puede
	 * abarcar 366 días— aunque la tabla de detalle del PDF muestre luego solo 40 filas.
	 *
	 * @param from inicio del rango, inclusivo
	 * @param to fin del rango, inclusivo
	 * @return los consumos del rango
	 */
	List<PartnerConsumption> findByConsumptionOpeningBetween(LocalDateTime from, LocalDateTime to);

	/**
	 * Consumos de un ambiente en un rango, con las entidades completas y sin paginar.
	 *
	 * <p>Variante de {@link #findByConsumptionOpeningBetween(LocalDateTime, LocalDateTime)} que aplica el
	 * reporte de consumos cuando se filtra por ambiente. Mismas consideraciones de volumen.
	 *
	 * @param enviroment nombre del ambiente, con coincidencia exacta
	 * @param from inicio del rango, inclusivo
	 * @param to fin del rango, inclusivo
	 * @return los consumos coincidentes
	 */
	List<PartnerConsumption> findByEnviromentAndConsumptionOpeningBetween(
			String enviroment, LocalDateTime from, LocalDateTime to);

	/**
	 * Consumos de un socio en un rango, con las entidades completas y sin paginar.
	 *
	 * <p>La usa {@link co.edu.unbosque.service.PartnerMetricsService}, que <strong>agrega en memoria</strong>
	 * en lugar de en SQL: es la única familia de métricas del sistema que lo hace. No usa la proyección
	 * {@link ConsumptionRowView} ni impone tope alguno, de modo que un socio con mucho historial y un rango
	 * amplio hidratan todas sus entidades para calcular unas cuantas sumas.
	 *
	 * @param personId clave primaria del socio
	 * @param from inicio del rango, inclusivo
	 * @param to fin del rango, inclusivo
	 * @return los consumos del socio en el rango
	 */
	List<PartnerConsumption> findByPartnerPersonIdAndConsumptionOpeningBetween(
			Long personId, LocalDateTime from, LocalDateTime to);

	/**
	 * Agrega los importes y el número de cargos de un rango.
	 *
	 * <p>Los {@code COALESCE} son necesarios porque todas las columnas monetarias admiten nulos; sin ellos
	 * una sola fila incompleta anularía la suma entera.
	 *
	 * <p><strong>Contrato posicional del resultado</strong>, que solo conoce quien lo consume:
	 * {@code [0]} suma de {@code consumptionValue}, {@code [1]} de {@code iva}, {@code [2]} de
	 * {@code service}, {@code [3]} de {@code tip}, {@code [4]} número de cargos. El total facturado se
	 * obtiene sumando los cuatro primeros.
	 *
	 * <p>Nótese que esta consulta <strong>no menciona al socio</strong>, de modo que incluye los consumos
	 * sin socio asociado. {@link #occupancyByEnvironment(LocalDateTime, LocalDateTime)} sí lo menciona y
	 * los excluye: dos métricas del mismo periodo pueden por tanto discrepar.
	 *
	 * @param from inicio del rango, inclusivo
	 * @param to fin del rango, inclusivo
	 * @return una única fila con las cinco agregaciones
	 */
	@Query("SELECT COALESCE(SUM(c.consumptionValue),0), COALESCE(SUM(c.iva),0), " +
			"COALESCE(SUM(c.service),0), COALESCE(SUM(c.tip),0), COUNT(c) " +
			"FROM PartnerConsumption c WHERE c.consumptionOpening BETWEEN :from AND :to")
	List<Object[]> aggregateSummary(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

	/**
	 * Agrega el total facturado y el número de cargos por ambiente, de mayor a menor facturación.
	 *
	 * <p>Aquí la fórmula del total —{@code consumptionValue + iva + service + tip}— está escrita
	 * <strong>en SQL</strong>. Es una de las siete implementaciones independientes de la misma regla
	 * financiera que existen en el sistema, y la única que no es código Java.
	 *
	 * <p>Contrato posicional: {@code [0]} ambiente, {@code [1]} total facturado, {@code [2]} número de
	 * cargos. Los porcentajes sobre el total global los calcula el servicio, en una segunda pasada.
	 *
	 * @param from inicio del rango, inclusivo
	 * @param to fin del rango, inclusivo
	 * @return una fila por ambiente, ordenadas por total descendente
	 */
	@Query("SELECT c.enviroment, SUM(COALESCE(c.consumptionValue,0) + COALESCE(c.iva,0) + COALESCE(c.service,0) + COALESCE(c.tip,0)), COUNT(c) " +
			"FROM PartnerConsumption c WHERE c.consumptionOpening BETWEEN :from AND :to " +
			"GROUP BY c.enviroment ORDER BY SUM(COALESCE(c.consumptionValue,0) + COALESCE(c.iva,0) + COALESCE(c.service,0) + COALESCE(c.tip,0)) DESC")
	List<Object[]> aggregateByEnvironment(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

	/**
	 * Recupera solo la fecha y los cuatro importes de cada consumo del rango, sin hidratar entidades.
	 *
	 * <p>Es la única consulta del sistema que usa una <strong>proyección por interfaz</strong>
	 * ({@link ConsumptionRowView}). La usan los cálculos de tendencia y de horas pico, que necesitan
	 * recorrer fila por fila pero no requieren entidades gestionadas.
	 *
	 * <p>Los alias del {@code SELECT} <strong>deben coincidir con los nombres de los getters</strong> de la
	 * interfaz de proyección: renombrar uno sin el otro rompe la consulta en ejecución, no en compilación.
	 *
	 * <p>Compárese con {@code PartnerMetricsService}, que resuelve un problema equivalente hidratando las
	 * entidades completas.
	 *
	 * @param from inicio del rango, inclusivo
	 * @param to fin del rango, inclusivo
	 * @return las filas del rango, como proyecciones de solo lectura
	 */
	@Query("SELECT c.consumptionOpening AS consumptionOpening, c.consumptionValue AS consumptionValue, " +
			"c.iva AS iva, c.service AS service, c.tip AS tip " +
			"FROM PartnerConsumption c WHERE c.consumptionOpening BETWEEN :from AND :to")
	List<ConsumptionRowView> findRowsInRange(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

	/**
	 * Cuenta los socios <em>distintos</em> que consumieron en cada ambiente durante el rango.
	 *
	 * <p>Es lo que el sistema entiende por «ocupación», y conviene ser preciso: mide <strong>actividad de
	 * consumo, no presencia física</strong>. Aunque la sirve un endpoint de métricas de acceso, no consulta
	 * la tabla {@code access} en ningún momento.
	 *
	 * <p><strong>Unión implícita.</strong> Referenciar {@code c.partner.personId} genera un <em>inner
	 * join</em>, de modo que esta consulta <strong>excluye</strong> los consumos cuya clave ajena es nula,
	 * a diferencia de {@link #aggregateSummary(LocalDateTime, LocalDateTime)}.
	 *
	 * <p>Contrato posicional: {@code [0]} ambiente, {@code [1]} número de socios distintos.
	 *
	 * @param from inicio del rango, inclusivo
	 * @param to fin del rango, inclusivo
	 * @return una fila por ambiente, ordenadas por número de socios descendente
	 */
	@Query("SELECT c.enviroment, COUNT(DISTINCT c.partner.personId) FROM PartnerConsumption c " +
			"WHERE c.consumptionOpening BETWEEN :from AND :to GROUP BY c.enviroment " +
			"ORDER BY COUNT(DISTINCT c.partner.personId) DESC")
	List<Object[]> occupancyByEnvironment(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

	/**
	 * Fecha del consumo más antiguo registrado.
	 *
	 * <p>Determina el punto de partida del relleno de resúmenes mensuales: {@code backfillMissing()}
	 * recorre los meses desde aquí hasta el mes anterior al actual.
	 *
	 * @return la fecha de apertura más antigua, o {@code null} si no hay ningún consumo. Ese nulo es la
	 *         condición de salida temprana del relleno sobre una base vacía
	 */
	@Query("SELECT MIN(c.consumptionOpening) FROM PartnerConsumption c")
	LocalDateTime findEarliestConsumption();

}