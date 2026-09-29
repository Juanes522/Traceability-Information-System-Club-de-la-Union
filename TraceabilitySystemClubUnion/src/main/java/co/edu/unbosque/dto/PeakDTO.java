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

	/**
	 * Constructor sin argumentos requerido para la deserialización del cuerpo de la petición.
	 */
	public PeakDTO() {
	}

	/**
	 * Crea una instancia con sus valores.
	 *
	 * @param byHour la serie por hora del día
	 * @param byWeekday la serie por día de la semana
	 */
	public PeakDTO(List<HourBucketDTO> byHour, List<WeekdayBucketDTO> byWeekday) {
		this.byHour = byHour;
		this.byWeekday = byWeekday;
	}

	/**
	 * Devuelve la serie por hora del día.
	 *
	 * @return la serie por hora del día
	 */
	public List<HourBucketDTO> getByHour() {
		return byHour;
	}

	/**
	 * Establece la serie por hora del día.
	 *
	 * @param v la serie por hora del día
	 */
	public void setByHour(List<HourBucketDTO> v) {
		this.byHour = v;
	}

	/**
	 * Devuelve la serie por día de la semana.
	 *
	 * @return la serie por día de la semana
	 */
	public List<WeekdayBucketDTO> getByWeekday() {
		return byWeekday;
	}

	/**
	 * Establece la serie por día de la semana.
	 *
	 * @param v la serie por día de la semana
	 */
	public void setByWeekday(List<WeekdayBucketDTO> v) {
		this.byWeekday = v;
	}
}
