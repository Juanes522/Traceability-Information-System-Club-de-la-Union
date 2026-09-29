package co.edu.unbosque.dto;

import java.time.LocalDateTime;

/**
 * Proyección de solo lectura con la fecha y los cuatro importes de un consumo.
 *
 * <p>Es la <strong>única proyección por interfaz</strong> del sistema, y existe por rendimiento: los cálculos de tendencia
 * y de concentración horaria necesitan recorrer fila por fila, pero no requieren entidades gestionadas ni el resto de las
 * columnas.
 *
 * <p>Los nombres de estos métodos deben coincidir con los alias de la consulta que la produce. Renombrar uno sin el otro
 * rompe la consulta <strong>en ejecución</strong>, no al compilar.
 *
 * <p>No se serializa nunca hacia el cliente: es de uso interno.
 */
public interface ConsumptionRowView {
	/**
	 * Devuelve el momento de apertura del consumo, que es el campo por el que se agrupan las series.
	 *
	 * <p>El nombre de este metodo debe coincidir con el alias de la consulta que produce la proyeccion.
	 *
	 * @return el momento de apertura del consumo, que es el campo por el que se agrupan las series
	 */
	LocalDateTime getConsumptionOpening();
	/**
	 * Devuelve el valor neto del consumo.
	 *
	 * <p>El nombre de este metodo debe coincidir con el alias de la consulta que produce la proyeccion.
	 *
	 * @return el valor neto del consumo
	 */
	Double getConsumptionValue();
	/**
	 * Devuelve el impuesto al valor agregado.
	 *
	 * <p>El nombre de este metodo debe coincidir con el alias de la consulta que produce la proyeccion.
	 *
	 * @return el impuesto al valor agregado
	 */
	Double getIva();
	/**
	 * Devuelve el recargo por servicio.
	 *
	 * <p>El nombre de este metodo debe coincidir con el alias de la consulta que produce la proyeccion.
	 *
	 * @return el recargo por servicio
	 */
	Double getService();
	/**
	 * Devuelve la propina.
	 *
	 * <p>El nombre de este metodo debe coincidir con el alias de la consulta que produce la proyeccion.
	 *
	 * @return la propina
	 */
	Double getTip();
}
