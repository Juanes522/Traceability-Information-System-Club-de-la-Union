package co.edu.unbosque.dto;

import java.util.List;

/**
 * Concentración de la facturación por hora del día y por día de la semana.
 *
 * <p>Responde a cuándo se concentra la actividad, a efectos de dotación de personal. Agrega las dos dimensiones
 * <strong>por separado</strong>, a diferencia del mapa de calor, que las cruza.
 *
 * <p>Ambas series llegan completas, con ceros incluidos. Ninguna pantalla lo consume: el tablero usa el mapa de calor.
 */
public class PeakDTO {

	private List<HourBucketDTO> byHour;
	private List<WeekdayBucketDTO> byWeekday;

	public PeakDTO() {
	}

	public PeakDTO(List<HourBucketDTO> byHour, List<WeekdayBucketDTO> byWeekday) {
		this.byHour = byHour;
		this.byWeekday = byWeekday;
	}

	public List<HourBucketDTO> getByHour() {
		return byHour;
	}

	public void setByHour(List<HourBucketDTO> v) {
		this.byHour = v;
	}

	public List<WeekdayBucketDTO> getByWeekday() {
		return byWeekday;
	}

	public void setByWeekday(List<WeekdayBucketDTO> v) {
		this.byWeekday = v;
	}
}
