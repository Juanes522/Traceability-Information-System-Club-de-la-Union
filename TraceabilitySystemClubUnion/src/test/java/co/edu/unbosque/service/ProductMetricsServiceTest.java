package co.edu.unbosque.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.edu.unbosque.dto.CategoryMixDTO;
import co.edu.unbosque.dto.EnvironmentCategoryDTO;
import co.edu.unbosque.dto.ProductDetailDTO;
import co.edu.unbosque.dto.ProductRankDTO;
import co.edu.unbosque.repository.ConsumptionItemRepository;

@ExtendWith(MockitoExtension.class)
class ProductMetricsServiceTest {

	@Mock ConsumptionItemRepository repo;
	@InjectMocks ProductMetricsService service;

	private final LocalDateTime FROM = LocalDateTime.of(2026,5,1,0,0);
	private final LocalDateTime TO = LocalDateTime.of(2026,5,31,23,59);

	@Test
	void top_mapsRowsAndAppliesLimit() {
		when(repo.topProducts(FROM, TO)).thenReturn(List.of(
			new Object[]{"P1", "VINO", 3L, 30.0},
			new Object[]{"P2", "ENTRADA", 1L, 10.0}));
		List<ProductRankDTO> out = service.top(FROM, TO, 1);
		assertThat(out).hasSize(1);
		assertThat(out.get(0).getProductId()).isEqualTo("P1");
		assertThat(out.get(0).getRevenue()).isEqualTo(30.0);
		assertThat(out.get(0).getQuantity()).isEqualTo(3L);
	}

	@Test
	void top_conAmbiente_usaLaQueryFiltrada() {
		when(repo.topProductsInEnvironment("Rooftop", FROM, TO)).thenReturn(List.<Object[]>of(new Object[]{"PX","COCTEL",5L,40.0}));
		List<ProductRankDTO> out = service.top(FROM, TO, "Rooftop", 20);
		assertThat(out).hasSize(1);
		assertThat(out.get(0).getProductId()).isEqualTo("PX");
		verify(repo).topProductsInEnvironment("Rooftop", FROM, TO);
		verify(repo, never()).topProducts(any(), any());
	}

	@Test
	void top_sinAmbiente_usaLaQueryGlobal() {
		when(repo.topProducts(FROM, TO)).thenReturn(List.<Object[]>of(new Object[]{"P1","VINO",3L,30.0}));
		service.top(FROM, TO, "", 20);
		verify(repo).topProducts(FROM, TO);
		verify(repo, never()).topProductsInEnvironment(any(), any(), any());
	}

	@Test
	void top5arg_sinAmbienteConSortQuantity_usaTopProductsByQuantity() {
		when(repo.topProductsByQuantity(FROM, TO)).thenReturn(List.<Object[]>of(new Object[]{"P2","CERVEZA",10L,20.0}));
		List<ProductRankDTO> out = service.top(FROM, TO, "", "quantity", 20);
		assertThat(out).hasSize(1);
		assertThat(out.get(0).getProductId()).isEqualTo("P2");
		verify(repo).topProductsByQuantity(FROM, TO);
		verify(repo, never()).topProducts(any(), any());
	}

	@Test
	void top5arg_conAmbienteConSortQuantity_usaTopProductsInEnvironmentByQuantity() {
		when(repo.topProductsInEnvironmentByQuantity("Rooftop", FROM, TO))
				.thenReturn(List.<Object[]>of(new Object[]{"PX","COCTEL",5L,40.0}));
		List<ProductRankDTO> out = service.top(FROM, TO, "Rooftop", "quantity", 20);
		assertThat(out).hasSize(1);
		verify(repo).topProductsInEnvironmentByQuantity("Rooftop", FROM, TO);
		verify(repo, never()).topProductsInEnvironment(any(), any(), any());
	}

	@Test
	void top5arg_sortRevenue_delegatesToExistingQueries() {
		when(repo.topProducts(FROM, TO)).thenReturn(List.<Object[]>of(new Object[]{"P1","VINO",3L,30.0}));
		service.top(FROM, TO, "", "revenue", 20);
		verify(repo).topProducts(FROM, TO);
		verify(repo, never()).topProductsByQuantity(any(), any());
	}

	@Test
	void top4arg_delegatesToRevenueSort() {
		when(repo.topProducts(FROM, TO)).thenReturn(List.<Object[]>of(new Object[]{"P1","VINO",3L,30.0}));
		List<ProductRankDTO> out = service.top(FROM, TO, "", 20);
		assertThat(out).hasSize(1);
		verify(repo).topProducts(FROM, TO);
	}

	@Test
	void categoryMix_computesPercentageOfRevenue() {
		when(repo.categoryMix(FROM, TO)).thenReturn(List.of(
			new Object[]{"BEBIDAS", "VINO TINTO", 3L, 30.0},
			new Object[]{"ALIMENTOS", "ENTRADAS FRIAS", 1L, 10.0}));
		List<CategoryMixDTO> out = service.categoryMix(FROM, TO);
		assertThat(out.get(0).getPercentage()).isEqualTo(75.0);
	}

	@Test
	void categoryMix_zeroRevenue_noDivisionByZero() {
		when(repo.categoryMix(FROM, TO)).thenReturn(List.<Object[]>of(new Object[]{"BEBIDAS", "X", 0L, 0.0}));
		assertThat(service.categoryMix(FROM, TO).get(0).getPercentage()).isEqualTo(0.0);
	}

	@Test
	void byEnvironmentCategory_mapsRows() {
		when(repo.byEnvironmentCategory(FROM, TO)).thenReturn(List.<Object[]>of(
			new Object[]{"Restaurante", "BEBIDAS", 3L, 30.0}));
		List<EnvironmentCategoryDTO> out = service.byEnvironmentCategory(FROM, TO);
		assertThat(out.get(0).getEnvironment()).isEqualTo("Restaurante");
	}

	@Test
	void productDetail_mapeaLasFilas() {
		when(repo.productDetailBySection(FROM, TO)).thenReturn(List.<Object[]>of(
			new Object[]{"BEBIDAS","VINO TINTO","P1","VINO",3L,30.0}));
		List<ProductDetailDTO> out = service.productDetail(FROM, TO);
		assertThat(out).hasSize(1);
		assertThat(out.get(0).getName()).isEqualTo("VINO");
		assertThat(out.get(0).getQuantity()).isEqualTo(3L);
		assertThat(out.get(0).getRevenue()).isEqualTo(30.0);
	}

	@Test
	void topByPartner_mapsRows() {
		when(repo.topProductsByPartner(7L, FROM, TO)).thenReturn(List.<Object[]>of(
			new Object[]{"P1", "VINO", 2L, 20.0}));
		assertThat(service.topByPartner(7L, FROM, TO, 20)).hasSize(1);
	}
}
