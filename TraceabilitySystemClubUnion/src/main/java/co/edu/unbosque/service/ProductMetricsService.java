package co.edu.unbosque.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import co.edu.unbosque.dto.CategoryMixDTO;
import co.edu.unbosque.dto.EnvironmentCategoryDTO;
import co.edu.unbosque.dto.ProductDetailDTO;
import co.edu.unbosque.dto.ProductRankDTO;
import co.edu.unbosque.repository.ConsumptionItemRepository;

/**
 * Analítica de productos: rankings, mezcla por categoría y cruce con ambientes.
 *
 * <p>Opera sobre las líneas de detalle de los consumos ({@link co.edu.unbosque.model.ConsumptionItem}), que son la única
 * fuente de información de producto del sistema. Alimenta los endpoints de {@code /metrics/products/*} y las tres
 * secciones de producto de los reportes PDF.
 *
 * <p>Agrega en SQL y solo desempaqueta y recorta en Java. <strong>El recorte al número de resultados solicitado ocurre
 * después</strong> de que la base haya materializado todos los grupos: no hay límite en la consulta, así que pedir los
 * diez primeros productos de un año cuesta lo mismo que pedirlos todos.
 *
 * <p>Sin catálogo de productos detrás, la agrupación se hace por el texto tal como se registró: dos grafías del mismo
 * producto producen dos filas distintas en los rankings.
 */
@Service
public class ProductMetricsService {

	private final ConsumptionItemRepository repo;

	/**
	 * Crea una instancia con sus valores.
	 *
	 * @param repo el valor de repo
	 */
	public ProductMetricsService(ConsumptionItemRepository repo) {
		this.repo = repo;
	}

	/**
	 * Ranking de productos por ingresos, sin filtrar por ambiente.
	 *
	 * <p>Es la variante que consume {@link ReportService} para la sección de productos más vendidos.
	 *
	 * @param from inicio del rango, inclusivo
	 * @param to fin del rango, inclusivo
	 * @param limit número máximo de productos; con cero o negativo devuelve una lista vacía
	 * @return los productos con más ingresos, en orden descendente
	 */
	public List<ProductRankDTO> top(LocalDateTime from, LocalDateTime to, int limit) {
		return rank(repo.topProducts(from, to), limit);
	}

	/**
	 * Ranking de productos por ingresos dentro de un ambiente. Sin uso actualmente.
	 *
	 * <p>Sobrecarga que delega en la variante de cinco argumentos fijando el orden por ingresos.
	 *
	 * @param from inicio del rango, inclusivo
	 * @param to fin del rango, inclusivo
	 * @param environment nombre exacto del ambiente
	 * @param limit número máximo de productos
	 * @return los productos con más ingresos en ese ambiente
	 */
	public List<ProductRankDTO> top(LocalDateTime from, LocalDateTime to, String environment, int limit) {
		return top(from, to, environment, "revenue", limit);
	}

	/**
	 * Ranking de productos, eligiendo criterio de orden y filtro de ambiente.
	 *
	 * <p>Es la variante que sirve {@code GET /metrics/products/top}. Combina dos decisiones binarias para escoger una de
	 * las cuatro consultas del repositorio: ordenar por cantidad o por ingresos, y filtrar o no por ambiente.
	 *
	 * <p><strong>Cualquier valor de {@code sort} distinto de {@code quantity} se interpreta silenciosamente como orden
	 * por ingresos</strong>, incluido un valor mal escrito: no hay validación ni error.
	 *
	 * @param from inicio del rango, inclusivo
	 * @param to fin del rango, inclusivo
	 * @param environment nombre exacto del ambiente, o {@code null}/vacío para no filtrar
	 * @param sort {@code quantity} para ordenar por unidades vendidas; cualquier otro valor ordena por ingresos
	 * @param limit número máximo de productos. <strong>El controlador no impone cota superior</strong>
	 * @return los productos, en el orden solicitado
	 */
	public List<ProductRankDTO> top(LocalDateTime from, LocalDateTime to, String environment, String sort, int limit) {
		boolean byQty = "quantity".equalsIgnoreCase(sort);
		boolean hasEnv = environment != null && !environment.isBlank();
		List<Object[]> rows;
		if (hasEnv) {
			rows = byQty ? repo.topProductsInEnvironmentByQuantity(environment, from, to)
					: repo.topProductsInEnvironment(environment, from, to);
		} else {
			rows = byQty ? repo.topProductsByQuantity(from, to) : repo.topProducts(from, to);
		}
		return rank(rows, limit);
	}

	/**
	 * Ranking de productos de un socio concreto, por ingresos.
	 *
	 * <p>Sirve tanto el panel del propio socio como la sección de productos de su estado de cuenta en PDF.
	 *
	 * @param personId clave primaria del socio
	 * @param from inicio del rango, inclusivo
	 * @param to fin del rango, inclusivo
	 * @param limit número máximo de productos
	 * @return los productos que más consumió, en orden descendente de importe
	 */
	public List<ProductRankDTO> topByPartner(Long personId, LocalDateTime from, LocalDateTime to, int limit) {
		return rank(repo.topProductsByPartner(personId, from, to), limit);
	}

	/**
	 * Distribución de ingresos por categoría y subcategoría, con su peso porcentual.
	 *
	 * <p>Como la distribución por ambiente, requiere dos pasadas: una para el total global y otra para los porcentajes.
	 *
	 * <p>No se recorta: devuelve todas las combinaciones de categoría y subcategoría del periodo.
	 *
	 * @param from inicio del rango, inclusivo
	 * @param to fin del rango, inclusivo
	 * @return la mezcla por categoría, ordenada por ingresos descendentes
	 */
	public List<CategoryMixDTO> categoryMix(LocalDateTime from, LocalDateTime to) {
		List<Object[]> rows = repo.categoryMix(from, to);
		double global = 0.0;
		for (Object[] r : rows) {
			global += num(r[3]);
		}
		List<CategoryMixDTO> out = new ArrayList<>();
		for (Object[] r : rows) {
			double rev = num(r[3]);
			double pct = global > 0 ? rev / global * 100.0 : 0.0;
			out.add(new CategoryMixDTO((String) r[0], (String) r[1], ((Number) r[2]).longValue(), rev, pct));
		}
		return out;
	}

	/**
	 * Detalle de todos los productos del periodo, conservando su clasificación y agrupados por sección.
	 *
	 * <p>Es la vista más granular de la analítica de producto. La consume únicamente {@link ReportService}: el orden de
	 * las filas —por categoría, subcategoría y luego ingresos— <strong>es</strong> la estructura de la sección
	 * correspondiente del PDF, que se compone recorriéndolas secuencialmente.
	 *
	 * <p>Ningún endpoint la expone.
	 *
	 * @param from inicio del rango, inclusivo
	 * @param to fin del rango, inclusivo
	 * @return el detalle por producto, agrupado por sección
	 */
	public List<ProductDetailDTO> productDetail(LocalDateTime from, LocalDateTime to) {
		List<ProductDetailDTO> out = new ArrayList<>();
		for (Object[] r : repo.productDetailBySection(from, to)) {
			out.add(new ProductDetailDTO((String) r[0], (String) r[1], (String) r[2], (String) r[3],
					((Number) r[4]).longValue(), num(r[5])));
		}
		return out;
	}

	/**
	 * Cruce de ambiente con categoría de producto.
	 *
	 * <p>Permite ver <strong>qué</strong> se consume en cada espacio del club, no solo cuánto se factura en él: distingue
	 * un bar donde predomina la bebida de un restaurante donde predomina la comida.
	 *
	 * @param from inicio del rango, inclusivo
	 * @param to fin del rango, inclusivo
	 * @return un elemento por par ambiente/categoría, ordenados por ingresos descendentes
	 */
	public List<EnvironmentCategoryDTO> byEnvironmentCategory(LocalDateTime from, LocalDateTime to) {
		List<EnvironmentCategoryDTO> out = new ArrayList<>();
		for (Object[] r : repo.byEnvironmentCategory(from, to)) {
			out.add(new EnvironmentCategoryDTO((String) r[0], (String) r[1], ((Number) r[2]).longValue(), num(r[3])));
		}
		return out;
	}

	private List<ProductRankDTO> rank(List<Object[]> rows, int limit) {
		List<ProductRankDTO> out = new ArrayList<>();
		for (Object[] r : rows) {
			if (out.size() >= limit) {
				break;
			}
			out.add(new ProductRankDTO((String) r[0], (String) r[1], ((Number) r[2]).longValue(), num(r[3])));
		}
		return out;
	}

	private double num(Object o) {
		return o == null ? 0.0 : ((Number) o).doubleValue();
	}
}
