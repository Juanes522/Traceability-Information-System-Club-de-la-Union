package co.edu.unbosque.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.service.SnapshotService;

@RestController
@RequestMapping("/metrics/snapshots")
/**
 * Endpoint de consulta de los resúmenes mensuales precalculados.
 *
 * <p>Un solo método de lectura. Los resúmenes los produce {@link co.edu.unbosque.service.SnapshotService} mediante un
 * trabajo programado y un relleno al arrancar; aquí solo se exponen.
 *
 * <p>Sirve las series históricas de largo plazo del tablero, que sin estos agregados obligarían a recorrer todo el
 * histórico de consumos en cada consulta.
 *
 * @see co.edu.unbosque.service.SnapshotService
 * @see co.edu.unbosque.model.MonthlySnapshot
 */
public class SnapshotController {

	private final SnapshotService snapshotService;

	public SnapshotController(SnapshotService snapshotService) {
		this.snapshotService = snapshotService;
	}

	@PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
	/**
	 * Devuelve la serie histórica completa de resúmenes mensuales, en orden cronológico.
	 *
	 * <p>No está paginado ni acepta rango: devuelve todos los meses registrados. Es aceptable porque la tabla crece a razón
	 * de doce filas por año.
	 *
	 * <p>La proyección es <strong>parcial</strong>: omite el desglose monetario y la fecha de generación que sí se
	 * persisten.
	 *
	 * @return {@code 200} con todos los resúmenes, del mes más antiguo al más reciente
	 */
	@GetMapping
	public ResponseEntity<?> snapshots() {
		return ResponseEntity.ok(snapshotService.list());
	}
}
