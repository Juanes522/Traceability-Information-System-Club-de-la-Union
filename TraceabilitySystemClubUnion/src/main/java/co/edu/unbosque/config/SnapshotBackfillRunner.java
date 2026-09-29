package co.edu.unbosque.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import co.edu.unbosque.service.SnapshotService;

/**
 * Calcula al arrancar los resúmenes mensuales que falten.
 *
 * <p>Cubre los huecos que el trabajo programado no puede llenar por sí solo: el histórico anterior a la puesta en marcha del
 * sistema, y los meses perdidos si la aplicación estuvo detenida un día primero.
 *
 * <p>Características de su ejecución que conviene conocer:
 *
 * <ul>
 *   <li><strong>Se ejecuta en cada arranque, sin guarda alguna:</strong> no hay condición por perfil ni por propiedad. La
 *       idempotencia depende por completo del servicio, que omite los meses ya calculados.</li>
 *   <li><strong>Captura toda excepción y la escribe en la salida de error estándar</strong>, sin la traza. Un relleno que
 *       falle no impide el arranque —lo cual es deseable—, pero tampoco queda registrado de forma estructurada.</li>
 *   <li>A diferencia del método principal de la aplicación, este gancho <strong>se ejecuta igual en ambos modos de
 *       despliegue</strong>, con servidor embebido o como artefacto web en un contenedor externo.</li>
 * </ul>
 *
 * @see co.edu.unbosque.service.SnapshotService#backfillMissing()
 */
@Component
public class SnapshotBackfillRunner implements ApplicationRunner {

	private final SnapshotService snapshotService;

	/**
	 * Crea una instancia con sus valores.
	 *
	 * @param snapshotService el valor de snapshot service
	 */
	public SnapshotBackfillRunner(SnapshotService snapshotService) {
		this.snapshotService = snapshotService;
	}

	@Override
	public void run(ApplicationArguments args) {
		try {
			snapshotService.backfillMissing();
		} catch (Exception e) {
			System.err.println("Snapshot backfill failed: " + e.getMessage());
		}
	}
}
