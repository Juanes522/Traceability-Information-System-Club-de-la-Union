package co.edu.unbosque.repository;

import co.edu.unbosque.model.ConsumptionItem;
import co.edu.unbosque.model.PartnerConsumption;
import co.edu.unbosque.model.PersonPartner;
import co.edu.unbosque.security.AesGcmEncryptionService;
import co.edu.unbosque.security.DeterministicEncryptionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({ AesGcmEncryptionService.class, DeterministicEncryptionService.class })
class ConsumptionItemRepositoryTest {

	@Autowired
	private TestEntityManager em;

	@Autowired
	private ConsumptionItemRepository itemRepo;

	private PersonPartner persistPartner(String identification, Long shareNumber) {
		PersonPartner partner = new PersonPartner();
		partner.setIdentification(identification);
		partner.setShareNumber(shareNumber);
		return em.persistAndFlush(partner);
	}

	private ConsumptionItem item(String id, String name, int qty, double price, String cat, String sub, String dish) {
		ConsumptionItem it = new ConsumptionItem();
		it.setProductId(id); it.setName(name); it.setQuantity(qty); it.setUnitPrice(price);
		it.setLineTotal(price * qty); it.setCategory(cat); it.setSubcategory(sub); it.setDishType(dish);
		return it;
	}

	private Long seedTwoConsumptionsWithItems() {
		PersonPartner partner = persistPartner("0002", 2L);

		PartnerConsumption c1 = new PartnerConsumption();
		c1.setPartner(partner);
		c1.setEnviroment("Restaurante");
		c1.setConsumptionValue(30.0);
		c1.setIva(4.5); c1.setService(3.0); c1.setTip(0.0);
		c1.setConsumptionOpening(LocalDateTime.of(2026, 5, 12, 14, 0));
		c1.addItem(item("P1", "VINO", 2, 10.0, "BEBIDAS", "VINO TINTO", "BEBIDA"));
		c1.addItem(item("P2", "ENTRADA", 1, 10.0, "ALIMENTOS", "ENTRADAS FRIAS", "ENTRADA"));
		em.persist(c1);

		PartnerConsumption c2 = new PartnerConsumption();
		c2.setPartner(partner);
		c2.setEnviroment("Restaurante");
		c2.setConsumptionValue(10.0);
		c2.setIva(1.5); c2.setService(1.0); c2.setTip(0.0);
		c2.setConsumptionOpening(LocalDateTime.of(2026, 5, 20, 14, 0));
		c2.addItem(item("P1", "VINO", 1, 10.0, "BEBIDAS", "VINO TINTO", "BEBIDA"));
		em.persist(c2);

		em.flush();
		em.clear();

		return partner.getPersonId();
	}

	@Test
	void persistsItemsByCascadeFromConsumption() {
		PersonPartner partner = persistPartner("0001", 1L);
		PartnerConsumption c = new PartnerConsumption();
		c.setPartner(partner);
		c.setEnviroment("Restaurante");
		c.setConsumptionValue(30.0);
		c.setIva(4.5); c.setService(3.0); c.setTip(0.0);
		c.setConsumptionOpening(LocalDateTime.of(2026, 5, 12, 14, 0));
		c.addItem(item("P1", "VINO", 2, 10.0, "BEBIDAS", "VINO TINTO", "BEBIDA"));
		c.addItem(item("P2", "ENTRADA", 1, 10.0, "ALIMENTOS", "ENTRADAS FRIAS", "ENTRADA"));
		em.persist(c);
		em.flush();
		em.clear();

		List<ConsumptionItem> items = itemRepo.findByConsumptionConsumptionId(c.getConsumptionId());
		assertThat(items).hasSize(2);
		assertThat(items).extracting(ConsumptionItem::getLineTotal).contains(20.0, 10.0);
	}

	@Test
	void topProducts_aggregatesQuantityAndRevenueDescending() {
		seedTwoConsumptionsWithItems();
		LocalDateTime from = LocalDateTime.of(2026, 5, 1, 0, 0);
		LocalDateTime to = LocalDateTime.of(2026, 5, 31, 23, 59);
		List<Object[]> rows = itemRepo.topProducts(from, to);
		assertThat(rows.get(0)[0]).isEqualTo("P1");
		assertThat(((Number) rows.get(0)[2]).longValue()).isEqualTo(3L);
		assertThat(((Number) rows.get(0)[3]).doubleValue()).isEqualTo(30.0);
	}

	@Test
	void categoryMix_groupsByCategoryAndSubcategory() {
		seedTwoConsumptionsWithItems();
		List<Object[]> rows = itemRepo.categoryMix(
			LocalDateTime.of(2026, 5, 1, 0, 0), LocalDateTime.of(2026, 5, 31, 23, 59));
		assertThat(rows).isNotEmpty();
	}

	@Test
	void byEnvironmentCategory_groupsByParentEnvironment() {
		seedTwoConsumptionsWithItems();
		List<Object[]> rows = itemRepo.byEnvironmentCategory(
			LocalDateTime.of(2026, 5, 1, 0, 0), LocalDateTime.of(2026, 5, 31, 23, 59));
		assertThat(rows).anySatisfy(r -> assertThat((String) r[0]).isNotBlank());
	}

	@Test
	void topProductsByQuantity_ordersByUnits() {
		PersonPartner partner = persistPartner("0004", 4L);

		PartnerConsumption c1 = new PartnerConsumption();
		c1.setPartner(partner);
		c1.setEnviroment("Restaurante");
		c1.setConsumptionValue(100.0);
		c1.setIva(0.0); c1.setService(0.0); c1.setTip(0.0);
		c1.setConsumptionOpening(LocalDateTime.of(2026, 5, 12, 14, 0));
		c1.addItem(item("PHIGHREV", "CHAMPAGNE", 1, 100.0, "BEBIDAS", "VINO TINTO", "BEBIDA"));
		em.persist(c1);

		PartnerConsumption c2 = new PartnerConsumption();
		c2.setPartner(partner);
		c2.setEnviroment("Restaurante");
		c2.setConsumptionValue(20.0);
		c2.setIva(0.0); c2.setService(0.0); c2.setTip(0.0);
		c2.setConsumptionOpening(LocalDateTime.of(2026, 5, 13, 14, 0));
		c2.addItem(item("PHIGHQTY", "CERVEZA", 10, 2.0, "BEBIDAS", "CERVEZA", "BEBIDA"));
		em.persist(c2);

		em.flush();
		em.clear();

		LocalDateTime from = LocalDateTime.of(2026, 5, 1, 0, 0);
		LocalDateTime to = LocalDateTime.of(2026, 5, 31, 23, 59);
		List<Object[]> byRevenue = itemRepo.topProducts(from, to);
		assertThat(byRevenue.get(0)[0]).isEqualTo("PHIGHREV");

		List<Object[]> byQuantity = itemRepo.topProductsByQuantity(from, to);
		assertThat(byQuantity.get(0)[0]).isEqualTo("PHIGHQTY");
		assertThat(((Number) byQuantity.get(0)[2]).longValue()).isEqualTo(10L);
	}

	@Test
	void topProductsInEnvironmentByQuantity_filtraPorAmbienteYOrdenaPorUnidades() {
		PersonPartner partner = persistPartner("0005", 5L);

		PartnerConsumption c1 = new PartnerConsumption();
		c1.setPartner(partner);
		c1.setEnviroment("Rooftop");
		c1.setConsumptionValue(100.0);
		c1.setIva(0.0); c1.setService(0.0); c1.setTip(0.0);
		c1.setConsumptionOpening(LocalDateTime.of(2026, 5, 12, 14, 0));
		c1.addItem(item("PHIGHREV", "CHAMPAGNE", 1, 100.0, "BEBIDAS", "VINO TINTO", "BEBIDA"));
		em.persist(c1);

		PartnerConsumption c2 = new PartnerConsumption();
		c2.setPartner(partner);
		c2.setEnviroment("Rooftop");
		c2.setConsumptionValue(20.0);
		c2.setIva(0.0); c2.setService(0.0); c2.setTip(0.0);
		c2.setConsumptionOpening(LocalDateTime.of(2026, 5, 13, 14, 0));
		c2.addItem(item("PHIGHQTY", "CERVEZA", 10, 2.0, "BEBIDAS", "CERVEZA", "BEBIDA"));
		em.persist(c2);

		PartnerConsumption c3 = new PartnerConsumption();
		c3.setPartner(partner);
		c3.setEnviroment("Restaurante");
		c3.setConsumptionValue(50.0);
		c3.setIva(0.0); c3.setService(0.0); c3.setTip(0.0);
		c3.setConsumptionOpening(LocalDateTime.of(2026, 5, 14, 14, 0));
		c3.addItem(item("POTHERENV", "AGUA", 20, 2.5, "BEBIDAS", "AGUA", "BEBIDA"));
		em.persist(c3);

		em.flush();
		em.clear();

		LocalDateTime from = LocalDateTime.of(2026, 5, 1, 0, 0);
		LocalDateTime to = LocalDateTime.of(2026, 5, 31, 23, 59);
		List<Object[]> rows = itemRepo.topProductsInEnvironmentByQuantity("Rooftop", from, to);
		assertThat(rows).extracting(r -> r[0]).doesNotContain("POTHERENV");
		assertThat(rows.get(0)[0]).isEqualTo("PHIGHQTY");
	}

	@Test
	void topProductsInEnvironment_filtraPorAmbiente() {
		seedTwoConsumptionsWithItems();
		PersonPartner p = persistPartner("0003", 3L);
		PartnerConsumption c = new PartnerConsumption();
		c.setPartner(p); c.setEnviroment("Rooftop");
		c.setConsumptionValue(10.0); c.setIva(0.0); c.setService(0.0); c.setTip(0.0);
		c.setConsumptionOpening(LocalDateTime.of(2026, 5, 15, 20, 0));
		c.addItem(item("PX", "COCTEL", 5, 8.0, "BEBIDAS", "COCTELES", "PREPARAR Y SERVIR"));
		em.persist(c); em.flush(); em.clear();

		LocalDateTime from = LocalDateTime.of(2026, 5, 1, 0, 0);
		LocalDateTime to = LocalDateTime.of(2026, 5, 31, 23, 59);
		List<Object[]> rooftop = itemRepo.topProductsInEnvironment("Rooftop", from, to);
		assertThat(rooftop).hasSize(1);
		assertThat(rooftop.get(0)[0]).isEqualTo("PX");
		List<Object[]> rest = itemRepo.topProductsInEnvironment("Restaurante", from, to);
		assertThat(rest).extracting(r -> r[0]).doesNotContain("PX");
	}

	@Test
	void productDetailBySection_agrupaPorSeccionYProducto() {
		seedTwoConsumptionsWithItems();
		List<Object[]> rows = itemRepo.productDetailBySection(
			LocalDateTime.of(2026,5,1,0,0), LocalDateTime.of(2026,5,31,23,59));
		assertThat(rows).isNotEmpty();
		assertThat(rows.get(0)).hasSize(6);
	}

	@Test
	void topProductsByPartner_filtersByPerson() {
		Long personId = seedTwoConsumptionsWithItems();
		List<Object[]> rows = itemRepo.topProductsByPartner(personId,
			LocalDateTime.of(2026, 5, 1, 0, 0), LocalDateTime.of(2026, 5, 31, 23, 59));
		assertThat(rows).isNotEmpty();
	}
}
