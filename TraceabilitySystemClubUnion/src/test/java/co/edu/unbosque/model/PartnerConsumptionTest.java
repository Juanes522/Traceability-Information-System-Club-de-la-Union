package co.edu.unbosque.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PartnerConsumptionTest {

	@Test
	void getShareNumber_devuelveLaAccionDelSocio() {
		PersonPartner p = new PersonPartner();
		p.setShareNumber(121L);
		PartnerConsumption c = new PartnerConsumption();
		c.setPartner(p);
		assertThat(c.getShareNumber()).isEqualTo(121L);
	}

	@Test
	void getShareNumber_nullSiNoHayPartner() {
		assertThat(new PartnerConsumption().getShareNumber()).isNull();
	}
}
