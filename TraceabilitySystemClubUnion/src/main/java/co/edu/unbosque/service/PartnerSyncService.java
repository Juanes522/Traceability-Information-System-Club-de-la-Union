package co.edu.unbosque.service;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import co.edu.unbosque.dto.ExternalSocioDTO;
import co.edu.unbosque.dto.SyncResultDTO;
import co.edu.unbosque.model.PersonPartner;

/**
 * Sincroniza el padrón de socios con el maestro externo del club.
 *
 * <p>Es la <strong>única integración saliente</strong> del sistema: obtiene un arreglo JSON de
 * {@code ${external.socios.url}} y lo vuelca sobre la tabla de socios, creando los que falten y actualizando los
 * datos demográficos de los existentes.
 *
 * <p>Resuelve el problema de que el club ya tiene un sistema de socios anterior: en lugar de capturar de nuevo el
 * padrón, se importa. Por eso el reparto de responsabilidades es asimétrico: el feed es dueño de los datos
 * demográficos, y el backend es dueño de la contraseña, el rol, el estado y el consentimiento, que el feed no
 * conoce.
 *
 * <p>Lo dispara únicamente {@code POST /personpartner/sync}, reservado al rol {@code ADMIN}. No hay ninguna pantalla
 * que lo invoque: debe llamarse a la API directamente.
 *
 * <p>Advertencias sobre la robustez de esta integración:
 *
 * <ul>
 *   <li><strong>No es transaccional.</strong> El límite lo pone {@link PersonPartnerService}, así que hay una
 *       transacción por registro: un fallo a mitad del feed deja el padrón parcialmente sincronizado y descarta el
 *       resultado acumulado.</li>
 *   <li><strong>No genera evento de auditoría.</strong> No existe un tipo de evento para la sincronización, de modo
 *       que una mutación masiva de datos personales no deja rastro en la bitácora.</li>
 *   <li><strong>No hay baja lógica.</strong> Un socio retirado del feed conserva su estado activo indefinidamente.</li>
 *   <li>La llamada HTTP no tiene tiempo de espera, porque {@link co.edu.unbosque.config.RestClientConfig} sustituye
 *       el constructor autoconfigurado por uno desnudo.</li>
 * </ul>
 *
 * @see co.edu.unbosque.dto.ExternalSocioDTO
 * @see co.edu.unbosque.dto.SyncResultDTO
 */
@Service
public class PartnerSyncService {

	private final PersonPartnerService partnerService;
	private final RestClient restClient;
	private final String externalSociosUrl;
	private final PasswordEncoder passwordEncoder;

	/**
	 * Crea el servicio de sincronizacion.
	 *
	 * @param partnerService     acceso a los socios, que aporta ademas el limite transaccional por registro
	 * @param restClientBuilder  constructor de cliente HTTP; el que se inyecta no define tiempos de espera
	 * @param externalSociosUrl  direccion del maestro externo de socios
	 * @param passwordEncoder    codificador con el que se siembra la contrasena inicial de los socios nuevos
	 */
	public PartnerSyncService(PersonPartnerService partnerService, RestClient.Builder restClientBuilder,
			@Value("${external.socios.url}") String externalSociosUrl, PasswordEncoder passwordEncoder) {
		this.partnerService = partnerService;
		this.restClient = restClientBuilder.build();
		this.externalSociosUrl = externalSociosUrl;
		this.passwordEncoder = passwordEncoder;
	}

	/**
	 * Descarga el padrón externo completo y lo sincroniza.
	 *
	 * <p>Una sola petición {@code GET}, sin paginación, sin autenticación, sin reintento y <strong>sin tiempo de
	 * espera</strong>: si el servicio externo no responde, el hilo de la petición queda bloqueado indefinidamente.
	 *
	 * <p>Una respuesta nula se trata como lista vacía, de modo que un feed sin contenido no provoca error.
	 *
	 * @return el recuento de socios creados y actualizados
	 * @throws RuntimeException si la llamada falla o la respuesta no se puede deserializar. El controlador la captura
	 *                          y responde 502
	 */
	public SyncResultDTO sync() {
		ExternalSocioDTO[] externos = restClient.get().uri(externalSociosUrl).retrieve().body(ExternalSocioDTO[].class);
		return upsert(externos == null ? List.of() : Arrays.asList(externos));
	}

	/**
	 * Crea o actualiza los socios a partir de los registros del feed, emparejando por identificación.
	 *
	 * <p>Reparto de responsabilidades entre el feed y el backend:
	 *
	 * <ul>
	 *   <li><strong>Socio nuevo:</strong> se crea con rol {@code PARTNER} —sin prefijo, que
	 *       {@link co.edu.unbosque.model.PersonPartner#getRole()} normalizará—, estado activo, cambio de contraseña
	 *       forzado y <strong>la propia cédula como contraseña inicial</strong>, ya con hash. Esa contraseña es
	 *       deliberadamente predecible, y lo que la hace admisible es el cambio forzado en el primer acceso: sin él,
	 *       cualquiera que conozca una cédula podría entrar.</li>
	 *   <li><strong>Socio existente:</strong> se actualizan solo los datos demográficos. La contraseña, el rol, el
	 *       estado y el consentimiento <strong>no se tocan</strong>, porque el feed no es su dueño.</li>
	 * </ul>
	 *
	 * <p>Pérdidas de información conocidas en el mapeo:
	 *
	 * <ul>
	 *   <li>El feed trae <strong>una sola</strong> dirección de correo y <strong>reemplaza el arreglo completo</strong>
	 *       de la entidad, descartando cualquier dirección adicional que el socio tuviera.</li>
	 *   <li>El género llega como cadena y se reduce a su primer carácter.</li>
	 *   <li>Las fechas llegan como texto y se convierten con captura silenciosa: una fecha malformada se convierte en
	 *       nulo sin avisar.</li>
	 *   <li>No hay validación alguna en el DTO de entrada: un registro sin identificación se intentará persistir y
	 *       fallará contra la restricción de la base, sin error controlado.</li>
	 * </ul>
	 *
	 * @param externos registros del feed externo
	 * @return el recuento de creados y actualizados. <strong>No hay contador de fallos</strong>, de modo que los
	 *         errores parciales son invisibles para quien disparó la sincronización
	 */
	public SyncResultDTO upsert(List<ExternalSocioDTO> externos) {
		long created = 0;
		long updated = 0;
		for (ExternalSocioDTO e : externos) {
			PersonPartner p = partnerService.getByIdentification(e.getIdentification());
			if (p == null) {
				p = new PersonPartner();
				p.setRole("PARTNER");
				p.setPartnerState(true);
				p.setPassword(passwordEncoder.encode(e.getIdentification()));
				p.setForcePasswordChange(true);
				created++;
			} else {
				updated++;
			}
			p.setIdentification(e.getIdentification());
			p.setFirstName(e.getFirstName());
			p.setSecondName(e.getSecondName());
			p.setLastName(e.getLastName());
			if (e.getGender() != null && !e.getGender().isBlank()) {
				p.setGender(e.getGender().charAt(0));
			}
			p.setShareNumber(e.getShareNumber());
			if (e.getEmail() != null) {
				p.setEmail(new String[]{ e.getEmail() });
			}
			p.setPhone(e.getPhone());
			p.setCellPhone(e.getCellPhone());
			p.setBirthDate(parseDate(e.getBirthDate()));
			p.setIngressDate(parseDate(e.getIngressDate()));
			partnerService.savePartner(p);
		}
		return new SyncResultDTO(created, updated);
	}

	/**
	 * Convierte una fecha en texto, devolviendo {@code null} si no se puede interpretar.
	 *
	 * <p>Tolera datos sucios a propósito —un feed externo no es confiable— pero lo hace de forma
	 * <strong>silenciosa</strong>: no distingue entre «el socio no tiene fecha de ingreso» y «la fecha venía
	 * malformada», y no deja registro de cuántos valores se descartaron.
	 *
	 * @param s fecha en texto, en formato ISO
	 * @return la fecha, o {@code null} si es nula, vacía o no se puede interpretar
	 */
	private LocalDate parseDate(String s) {
		try {
			return (s == null || s.isBlank()) ? null : LocalDate.parse(s);
		} catch (Exception ex) {
			return null;
		}
	}
}
