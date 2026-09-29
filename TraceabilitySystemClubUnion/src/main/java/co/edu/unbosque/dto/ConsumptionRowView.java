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
	LocalDateTime getConsumptionOpening();
	Double getConsumptionValue();
	Double getIva();
	Double getService();
	Double getTip();
}
