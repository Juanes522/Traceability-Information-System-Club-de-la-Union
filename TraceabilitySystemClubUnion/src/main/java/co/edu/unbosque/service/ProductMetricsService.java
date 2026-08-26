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

@Service
public class ProductMetricsService {

	private final ConsumptionItemRepository repo;

	public ProductMetricsService(ConsumptionItemRepository repo) {
		this.repo = repo;
	}

	public List<ProductRankDTO> top(LocalDateTime from, LocalDateTime to, int limit) {
		return rank(repo.topProducts(from, to), limit);
	}

	public List<ProductRankDTO> top(LocalDateTime from, LocalDateTime to, String environment, int limit) {
		return top(from, to, environment, "revenue", limit);
	}

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

	public List<ProductRankDTO> topByPartner(Long personId, LocalDateTime from, LocalDateTime to, int limit) {
		return rank(repo.topProductsByPartner(personId, from, to), limit);
	}

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

	public List<ProductDetailDTO> productDetail(LocalDateTime from, LocalDateTime to) {
		List<ProductDetailDTO> out = new ArrayList<>();
		for (Object[] r : repo.productDetailBySection(from, to)) {
			out.add(new ProductDetailDTO((String) r[0], (String) r[1], (String) r[2], (String) r[3],
					((Number) r[4]).longValue(), num(r[5])));
		}
		return out;
	}

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
