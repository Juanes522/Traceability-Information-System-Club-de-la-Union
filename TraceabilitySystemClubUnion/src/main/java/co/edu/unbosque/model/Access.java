package co.edu.unbosque.model;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Presencia de un socio en el club: una visita, con su entrada y su salida.
 *
 * <p>Sostiene las métricas de afluencia de {@code /metrics/access/*}: visitas, socios distintos,
 * presentes en este momento y series de asistencia.
 *
 * <h2>La presencia se infiere del consumo, no se mide</h2>
 *
 * <p>No hay torniquete ni lector de acceso. Estas filas las crea
 * {@link co.edu.unbosque.service.AccessService#registerPresence(PersonPartner, LocalDateTime)} como
 * efecto secundario del registro de un consumo, usando la hora de apertura de la cuenta. Un socio
 * que entre al club y no consuma nada <strong>no aparece</strong> en esta tabla.
 *
 * <p>Conviene tenerlo presente al interpretar cualquier métrica de afluencia: mide actividad de
 * consumo, no presencia física.
 *
 * <h2>Invariante y su fragilidad</h2>
 *
 * <p>{@link co.edu.unbosque.service.AccessService} mantiene la regla «como máximo una visita abierta
 * por socio» mediante una comprobación previa a la inserción, <strong>sin índice único que la
 * respalde</strong>. Si dos registros concurrentes llegasen a crear dos visitas abiertas para el
 * mismo socio, la consulta que las busca —declarada con {@code Optional}— empezaría a fallar y el
 * registro de presencia de ese socio se detendría de forma silenciosa y permanente.
 */
@Entity
@Table(name = "access")
public class Access {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long accessId;

	/** Momento de entrada. Es el campo por el que se filtran y agrupan las métricas de afluencia. */
	private LocalDateTime dateTimeAdmission;
	/**
	 * Momento de salida, o {@code null} si el socio sigue dentro.
	 *
	 * <p><strong>{@code dateTimeDeparture IS NULL} es el predicado que significa «presente
	 * ahora»</strong>, y es el fundamento de todas las consultas de acceso del sistema.
	 *
	 * <p>Las visitas no se cierran de forma individual: un trabajo programado
	 * ({@link co.edu.unbosque.service.AccessService#closeOpenAccesses()}) estampa a las 02:00 la hora
	 * actual en <em>todas</em> las visitas abiertas, sin importar cuándo se abrieron. La duración
	 * resultante es por tanto una aproximación con sesgo sistemático, no una medición.
	 */
	private LocalDateTime dateTimeDeparture;

	/**
	 * Socio que realizó la visita. Lado propietario de la clave ajena, declarada
	 * {@code nullable = false}: a diferencia de {@code partner_consumption}, una visita no puede
	 * existir sin socio.
	 */
	@JsonIgnore
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "person_id", nullable = false)
	private PersonPartner partner;

	public Access() {
	}

	public Long getAccessId() {
		return accessId;
	}

	public void setAccessId(Long accessId) {
		this.accessId = accessId;
	}

	public LocalDateTime getDateTimeAdmission() {
		return dateTimeAdmission;
	}

	public void setDateTimeAdmission(LocalDateTime dateTimeAdmission) {
		this.dateTimeAdmission = dateTimeAdmission;
	}

	public LocalDateTime getDateTimeDeparture() {
		return dateTimeDeparture;
	}

	public void setDateTimeDeparture(LocalDateTime dateTimeDeparture) {
		this.dateTimeDeparture = dateTimeDeparture;
	}

	public PersonPartner getPartner() {
		return partner;
	}

	public void setPartner(PersonPartner partner) {
		this.partner = partner;
	}
}
