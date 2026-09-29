package co.edu.unbosque.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Verifica el número de acción derivado que expone la entidad de consumo.
 *
 * <p>Comprueba que devuelve la acción del socio y que tolera un consumo sin socio asociado. Importa porque ese valor derivado
 * es la <strong>única vía</strong> por la que el cliente conoce al propietario del cargo, ya que la asociación en sí no se
 * serializa.
 */
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
