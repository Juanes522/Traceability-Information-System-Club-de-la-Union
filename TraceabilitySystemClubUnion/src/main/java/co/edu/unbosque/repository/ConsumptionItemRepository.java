package co.edu.unbosque.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import co.edu.unbosque.model.ConsumptionItem;

public interface ConsumptionItemRepository extends JpaRepository<ConsumptionItem, Long> {
	List<ConsumptionItem> findByConsumptionConsumptionId(Long consumptionId);

	@Query("SELECT i.productId, i.name, SUM(i.quantity), SUM(i.lineTotal) "
			+ "FROM ConsumptionItem i JOIN i.consumption c "
			+ "WHERE c.consumptionOpening BETWEEN :from AND :to "
			+ "GROUP BY i.productId, i.name ORDER BY SUM(i.lineTotal) DESC")
	List<Object[]> topProducts(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

	@Query("SELECT i.productId, i.name, SUM(i.quantity), SUM(i.lineTotal) "
			+ "FROM ConsumptionItem i JOIN i.consumption c "
			+ "WHERE c.enviroment = :environment AND c.consumptionOpening BETWEEN :from AND :to "
			+ "GROUP BY i.productId, i.name ORDER BY SUM(i.lineTotal) DESC")
	List<Object[]> topProductsInEnvironment(@Param("environment") String environment,
			@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

	@Query("SELECT i.productId, i.name, SUM(i.quantity), SUM(i.lineTotal) "
			+ "FROM ConsumptionItem i JOIN i.consumption c "
			+ "WHERE c.consumptionOpening BETWEEN :from AND :to "
			+ "GROUP BY i.productId, i.name ORDER BY SUM(i.quantity) DESC")
	List<Object[]> topProductsByQuantity(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

	@Query("SELECT i.productId, i.name, SUM(i.quantity), SUM(i.lineTotal) "
			+ "FROM ConsumptionItem i JOIN i.consumption c "
			+ "WHERE c.enviroment = :environment AND c.consumptionOpening BETWEEN :from AND :to "
			+ "GROUP BY i.productId, i.name ORDER BY SUM(i.quantity) DESC")
	List<Object[]> topProductsInEnvironmentByQuantity(@Param("environment") String environment,
			@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

	@Query("SELECT i.category, i.subcategory, SUM(i.quantity), SUM(i.lineTotal) "
			+ "FROM ConsumptionItem i JOIN i.consumption c "
			+ "WHERE c.consumptionOpening BETWEEN :from AND :to "
			+ "GROUP BY i.category, i.subcategory ORDER BY SUM(i.lineTotal) DESC")
	List<Object[]> categoryMix(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

	@Query("SELECT c.enviroment, i.category, SUM(i.quantity), SUM(i.lineTotal) "
			+ "FROM ConsumptionItem i JOIN i.consumption c "
			+ "WHERE c.consumptionOpening BETWEEN :from AND :to "
			+ "GROUP BY c.enviroment, i.category ORDER BY SUM(i.lineTotal) DESC")
	List<Object[]> byEnvironmentCategory(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

	@Query("SELECT i.productId, i.name, SUM(i.quantity), SUM(i.lineTotal) "
			+ "FROM ConsumptionItem i JOIN i.consumption c "
			+ "WHERE c.partner.personId = :personId AND c.consumptionOpening BETWEEN :from AND :to "
			+ "GROUP BY i.productId, i.name ORDER BY SUM(i.lineTotal) DESC")
	List<Object[]> topProductsByPartner(@Param("personId") Long personId, @Param("from") LocalDateTime from,
			@Param("to") LocalDateTime to);

	@Query("SELECT i.category, i.subcategory, i.productId, i.name, SUM(i.quantity), SUM(i.lineTotal) "
			+ "FROM ConsumptionItem i JOIN i.consumption c "
			+ "WHERE c.consumptionOpening BETWEEN :from AND :to "
			+ "GROUP BY i.category, i.subcategory, i.productId, i.name "
			+ "ORDER BY i.category, i.subcategory, SUM(i.lineTotal) DESC")
	List<Object[]> productDetailBySection(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
}
