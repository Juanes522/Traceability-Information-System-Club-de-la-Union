package co.edu.unbosque.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import co.edu.unbosque.model.ConsumptionItem;

/**
 * Acceso a las líneas de detalle de los consumos: es el repositorio de la analítica de productos.
 *
 * <p>Salvo la primera consulta derivada, todos sus métodos son agregaciones JPQL que unen la línea con
 * su consumo para poder filtrar por fecha y por ambiente, y agrupan por distintas dimensiones del
 * producto.
 *
 * <h2>Convenciones comunes a todas las agregaciones</h2>
 *
 * <ul>
 *   <li>Todas devuelven {@code List<Object[]>} <strong>posicional</strong>: no hay proyección a DTO, y el
 *       significado de cada índice vive únicamente en {@code ProductMetricsService}. Renombrar o reordenar
 *       una columna rompe el consumidor en ejecución, no en compilación.</li>
 *   <li>El importe agregado es siempre {@code SUM(i.lineTotal)}, es decir la columna denormalizada que
 *       calculó el servicio al registrar el consumo. Nada garantiza su coherencia con el valor declarado
 *       del consumo padre.</li>
 *   <li><strong>Ninguna impone un límite en SQL.</strong> El recorte al número de resultados solicitado
 *       ocurre en Java, después de que la base haya materializado todos los grupos.</li>
 *   <li>{@code JOIN i.consumption c} es una unión interna, de modo que una línea sin consumo asociado
 *       queda fuera de toda la analítica.</li>
 *   <li>La agrupación es por el texto tal como se escribió: sin catálogo de productos ni de categorías,
 *       dos grafías del mismo producto producen dos filas distintas.</li>
 * </ul>
 *
 * <p>Nótese la asimetría de nomenclatura: los parámetros se llaman {@code environment} (grafía correcta)
 * mientras que la propiedad de la entidad es {@code c.enviroment}.
 */
public interface ConsumptionItemRepository extends JpaRepository<ConsumptionItem, Long> {
	/**
	 * Líneas de detalle de un consumo concreto. Sin uso actualmente.
	 *
	 * @param consumptionId clave primaria del consumo
	 * @return sus líneas de detalle
	 */
	List<ConsumptionItem> findByConsumptionConsumptionId(Long consumptionId);

	/**
	 * Ranking de productos por <strong>ingresos</strong> en un rango.
	 *
	 * <p>Es la variante base de una familia de cuatro consultas que combinan dos criterios de orden
	 * (ingresos o cantidad) con la presencia o ausencia de filtro por ambiente. El servicio elige una según
	 * los parámetros recibidos.
	 *
	 * <p>Contrato posicional: {@code [0]} identificador de producto, {@code [1]} nombre, {@code [2]} suma de
	 * cantidades, {@code [3]} suma de importes.
	 *
	 * @param from inicio del rango, inclusivo
	 * @param to fin del rango, inclusivo
	 * @return una fila por producto, ordenadas por ingresos descendentes y sin truncar
	 */
	@Query("SELECT i.productId, i.name, SUM(i.quantity), SUM(i.lineTotal) "
			+ "FROM ConsumptionItem i JOIN i.consumption c "
			+ "WHERE c.consumptionOpening BETWEEN :from AND :to "
			+ "GROUP BY i.productId, i.name ORDER BY SUM(i.lineTotal) DESC")
	List<Object[]> topProducts(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

	/**
	 * Ranking de productos por ingresos, restringido a un ambiente.
	 *
	 * <p>Mismo contrato posicional que {@link #topProducts(LocalDateTime, LocalDateTime)}. El ambiente se
	 * compara por igualdad exacta contra {@code c.enviroment}.
	 *
	 * @param environment nombre exacto del ambiente
	 * @param from inicio del rango, inclusivo
	 * @param to fin del rango, inclusivo
	 * @return una fila por producto, ordenadas por ingresos descendentes
	 */
	@Query("SELECT i.productId, i.name, SUM(i.quantity), SUM(i.lineTotal) "
			+ "FROM ConsumptionItem i JOIN i.consumption c "
			+ "WHERE c.enviroment = :environment AND c.consumptionOpening BETWEEN :from AND :to "
			+ "GROUP BY i.productId, i.name ORDER BY SUM(i.lineTotal) DESC")
	List<Object[]> topProductsInEnvironment(@Param("environment") String environment,
			@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

	/**
	 * Ranking de productos por <strong>cantidad vendida</strong> en un rango.
	 *
	 * <p>Idéntica a {@link #topProducts(LocalDateTime, LocalDateTime)} salvo el criterio de orden. Responde
	 * a una pregunta de negocio distinta: qué se vende más, frente a qué deja más ingresos.
	 *
	 * @param from inicio del rango, inclusivo
	 * @param to fin del rango, inclusivo
	 * @return una fila por producto, ordenadas por cantidad descendente
	 */
	@Query("SELECT i.productId, i.name, SUM(i.quantity), SUM(i.lineTotal) "
			+ "FROM ConsumptionItem i JOIN i.consumption c "
			+ "WHERE c.consumptionOpening BETWEEN :from AND :to "
			+ "GROUP BY i.productId, i.name ORDER BY SUM(i.quantity) DESC")
	List<Object[]> topProductsByQuantity(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

	/**
	 * Ranking de productos por cantidad, restringido a un ambiente.
	 *
	 * <p>Cuarta y última combinación de la familia de rankings: cantidad más filtro de ambiente.
	 *
	 * @param environment nombre exacto del ambiente
	 * @param from inicio del rango, inclusivo
	 * @param to fin del rango, inclusivo
	 * @return una fila por producto, ordenadas por cantidad descendente
	 */
	@Query("SELECT i.productId, i.name, SUM(i.quantity), SUM(i.lineTotal) "
			+ "FROM ConsumptionItem i JOIN i.consumption c "
			+ "WHERE c.enviroment = :environment AND c.consumptionOpening BETWEEN :from AND :to "
			+ "GROUP BY i.productId, i.name ORDER BY SUM(i.quantity) DESC")
	List<Object[]> topProductsInEnvironmentByQuantity(@Param("environment") String environment,
			@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

	/**
	 * Distribución de ingresos y cantidades por categoría y subcategoría.
	 *
	 * <p>Responde a la composición de la venta: qué peso tiene cada familia de producto. El porcentaje
	 * sobre el total lo calcula el servicio en una segunda pasada, no la consulta.
	 *
	 * <p>Contrato posicional: {@code [0]} categoría, {@code [1]} subcategoría, {@code [2]} suma de
	 * cantidades, {@code [3]} suma de importes.
	 *
	 * <p>Como la clasificación es texto libre, las líneas sin categoría se agrupan bajo un valor nulo que el
	 * servicio sustituye por una etiqueta genérica al presentarlas.
	 *
	 * @param from inicio del rango, inclusivo
	 * @param to fin del rango, inclusivo
	 * @return una fila por par categoría/subcategoría, ordenadas por ingresos descendentes
	 */
	@Query("SELECT i.category, i.subcategory, SUM(i.quantity), SUM(i.lineTotal) "
			+ "FROM ConsumptionItem i JOIN i.consumption c "
			+ "WHERE c.consumptionOpening BETWEEN :from AND :to "
			+ "GROUP BY i.category, i.subcategory ORDER BY SUM(i.lineTotal) DESC")
	List<Object[]> categoryMix(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

	/**
	 * Cruce de ambiente con categoría de producto.
	 *
	 * <p>Permite ver qué se consume en cada espacio del club, no solo cuánto se factura en él. Es la única
	 * agregación de este repositorio que agrupa por una dimensión del consumo y otra del producto a la vez.
	 *
	 * <p>Contrato posicional: {@code [0]} ambiente, {@code [1]} categoría, {@code [2]} suma de cantidades,
	 * {@code [3]} suma de importes.
	 *
	 * @param from inicio del rango, inclusivo
	 * @param to fin del rango, inclusivo
	 * @return una fila por par ambiente/categoría, ordenadas por ingresos descendentes
	 */
	@Query("SELECT c.enviroment, i.category, SUM(i.quantity), SUM(i.lineTotal) "
			+ "FROM ConsumptionItem i JOIN i.consumption c "
			+ "WHERE c.consumptionOpening BETWEEN :from AND :to "
			+ "GROUP BY c.enviroment, i.category ORDER BY SUM(i.lineTotal) DESC")
	List<Object[]> byEnvironmentCategory(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

	/**
	 * Ranking de productos de un socio concreto, por ingresos.
	 *
	 * <p>Sirve tanto el panel del propio socio ({@code /metrics/products/partner/me}) como su consulta por
	 * parte de un gestor, y alimenta la sección de productos del estado de cuenta en PDF.
	 *
	 * <p>Nótese que referencia {@code c.partner.personId}, lo que genera una <strong>unión interna
	 * adicional</strong>: los consumos sin socio quedan excluidos, como es lógico aquí.
	 *
	 * <p>Mismo contrato posicional que {@link #topProducts(LocalDateTime, LocalDateTime)}.
	 *
	 * @param personId clave primaria del socio
	 * @param from inicio del rango, inclusivo
	 * @param to fin del rango, inclusivo
	 * @return una fila por producto, ordenadas por ingresos descendentes
	 */
	@Query("SELECT i.productId, i.name, SUM(i.quantity), SUM(i.lineTotal) "
			+ "FROM ConsumptionItem i JOIN i.consumption c "
			+ "WHERE c.partner.personId = :personId AND c.consumptionOpening BETWEEN :from AND :to "
			+ "GROUP BY i.productId, i.name ORDER BY SUM(i.lineTotal) DESC")
	List<Object[]> topProductsByPartner(@Param("personId") Long personId, @Param("from") LocalDateTime from,
			@Param("to") LocalDateTime to);

	/**
	 * Detalle completo de producto, agrupado y ordenado por sección.
	 *
	 * <p>Es la agregación más fina del repositorio: llega hasta el producto individual pero conserva su
	 * clasificación, y ordena primero por categoría y subcategoría para que el resultado se pueda recorrer
	 * secuencialmente componiendo secciones. Por eso la consume la sección de desempeño de producto de los
	 * reportes PDF, donde el orden de las filas <em>es</em> la estructura del documento.
	 *
	 * <p>Contrato posicional, de seis columnas: {@code [0]} categoría, {@code [1]} subcategoría,
	 * {@code [2]} identificador de producto, {@code [3]} nombre, {@code [4]} suma de cantidades,
	 * {@code [5]} suma de importes.
	 *
	 * @param from inicio del rango, inclusivo
	 * @param to fin del rango, inclusivo
	 * @return una fila por producto, agrupadas por sección y ordenadas por ingresos dentro de cada una
	 */
	@Query("SELECT i.category, i.subcategory, i.productId, i.name, SUM(i.quantity), SUM(i.lineTotal) "
			+ "FROM ConsumptionItem i JOIN i.consumption c "
			+ "WHERE c.consumptionOpening BETWEEN :from AND :to "
			+ "GROUP BY i.category, i.subcategory, i.productId, i.name "
			+ "ORDER BY i.category, i.subcategory, SUM(i.lineTotal) DESC")
	List<Object[]> productDetailBySection(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
}
