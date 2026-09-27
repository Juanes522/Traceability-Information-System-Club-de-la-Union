package co.edu.unbosque.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import co.edu.unbosque.model.MonthlySnapshot;

/**
 * Acceso a los agregados mensuales precalculados.
 *
 * <p>Los tres métodos se corresponden exactamente con las tres necesidades de
 * {@link co.edu.unbosque.service.SnapshotService}: localizar una fila para actualizarla, comprobar si
 * un mes ya fue capturado, y leer la serie completa.
 */
public interface MonthlySnapshotRepository extends JpaRepository<MonthlySnapshot, Long> {

	/**
	 * Busca el resumen de un mes.
	 *
	 * <p>Sostiene el comportamiento de <em>upsert</em> de {@code SnapshotService.snapshotMonth}: si la
	 * fila existe se actualiza, y si no se crea. Recalcular un mes ya capturado sobrescribe sus valores.
	 *
	 * @param yearMonth mes en formato {@code YYYY-MM}
	 * @return el resumen, si ya fue calculado
	 */
	Optional<MonthlySnapshot> findByYearMonth(String yearMonth);

	/**
	 * Indica si un mes ya tiene resumen.
	 *
	 * <p>Es lo que hace idempotente el relleno de arranque: {@code backfillMissing()} solo calcula los
	 * meses ausentes. La contrapartida es que <strong>un mes capturado prematuramente, con datos
	 * incompletos, nunca se corrige</strong>, porque el relleno lo considera ya resuelto.
	 *
	 * @param yearMonth mes en formato {@code YYYY-MM}
	 * @return {@code true} si existe resumen para ese mes
	 */
	boolean existsByYearMonth(String yearMonth);

	/**
	 * Devuelve la serie histórica completa en orden cronológico.
	 *
	 * <p>El orden es lexicográfico sobre {@code yearMonth}, pero como el formato es {@code YYYY-MM} eso
	 * coincide con el orden cronológico, de modo que no hace falta convertir a fecha para ordenar.
	 *
	 * <p>No está paginado: devuelve todos los meses registrados. Es aceptable porque la tabla crece a
	 * razón de doce filas por año.
	 *
	 * @return todos los resúmenes, del mes más antiguo al más reciente
	 */
	List<MonthlySnapshot> findAllByOrderByYearMonthAsc();
}
