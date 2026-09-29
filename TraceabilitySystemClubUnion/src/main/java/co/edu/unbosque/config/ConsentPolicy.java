package co.edu.unbosque.config;

/**
 * Texto y versión de la política de tratamiento de datos personales.
 *
 * <p>Contenedor de constantes con la versión vigente, el título y el texto completo del aviso de privacidad, redactado en
 * español y estructurado en secciones: responsable del tratamiento, finalidades, seguridad, comunicación de datos, derechos
 * del titular y consentimiento.
 *
 * <p>La <strong>versión</strong> es el elemento funcionalmente activo: es el valor que se compara con el que cada socio
 * aceptó para decidir si debe volver a consentir.
 *
 * <p>Dos consideraciones sobre esta forma de almacenar la política:
 *
 * <ul>
 *   <li><strong>El texto está compilado.</strong> Cambiarlo exige recompilar y volver a desplegar; no es un parámetro de
 *       configuración ni un registro de base de datos.</li>
 *   <li><strong>La versión debe incrementarse a mano.</strong> Editar el texto sin cambiarla deja a todos los usuarios
 *       «consintiendo» un aviso que nunca vieron, lo que anula el propósito del versionado.</li>
 * </ul>
 *
 * <p>Nótese que el texto invoca la normativa de protección de datos personales del <strong>Ecuador</strong>, coherente con
 * que el club sea guayaquileño, mientras que el paquete y las coordenadas del proyecto corresponden a una universidad
 * colombiana. No es un defecto de código, pero es una discrepancia jurisdiccional aparente que conviene confirmar con el
 * responsable del proyecto.
 *
 * @see co.edu.unbosque.controller.AuthController#needsConsent(co.edu.unbosque.model.PersonPartner)
 */
public final class ConsentPolicy {

	/**
	 * Constante VERSION.
	 */
	public static final String VERSION = "1.0";

	/**
	 * Constante TITLE.
	 */
	public static final String TITLE = "Términos y Autorización para el tratamiento de datos personales";

	/**
	 * Constante TEXT.
	 */
	public static final String TEXT = """
			Al continuar y aceptar estos términos, usted declara haber recibido información clara, previa y suficiente sobre el tratamiento de sus datos personales y manifiesta de forma libre, específica, informada e inequívoca su consentimiento para que el Club de la Unión trate sus datos personales de conformidad con la Ley Orgánica de Protección de Datos Personales del Ecuador (LOPDP) y demás normativa aplicable.

			Responsable del tratamiento
			El Club de la Unión será el responsable del tratamiento de los datos personales registrados y utilizados mediante este aplicativo, determinando las finalidades y condiciones de dicho tratamiento.

			Finalidades del tratamiento
			Los datos personales podrán ser tratados para:
			- Registrar, consultar y realizar la trazabilidad de los consumos asociados a su acción.
			- Informar oportunamente sobre los cargos realizados a su acción.
			- Gestionar el control de accesos y la información relacionada con la operación del club.
			- Generar reportes, indicadores y estadísticas de uso para fines administrativos.
			- Atender solicitudes, consultas, reclamos e inconsistencias relacionadas con los consumos.
			- Mantener registros de auditoría y trazabilidad de las operaciones realizadas mediante el sistema.

			Seguridad y confidencialidad
			El Club de la Unión adoptará medidas técnicas, organizativas y de seguridad destinadas a proteger los datos personales frente a accesos, usos o tratamientos no autorizados, de acuerdo con la naturaleza y contexto de la información tratada.

			Comunicación de datos
			Los datos personales no serán comunicados a terceros para finalidades distintas de las informadas, salvo cuando exista autorización del titular, una obligación legal o una circunstancia permitida por la normativa aplicable.

			Derechos del titular
			De acuerdo con la normativa ecuatoriana aplicable, usted podrá ejercer los derechos reconocidos sobre sus datos personales, incluyendo el acceso, rectificación y actualización, eliminación, oposición, limitación cuando corresponda y revocatoria del consentimiento, mediante los canales habilitados por el Club de la Unión.

			Consentimiento
			La aceptación de esta autorización constituye una manifestación afirmativa de voluntad. Usted podrá solicitar información sobre el tratamiento realizado y, cuando corresponda, revocar su consentimiento mediante los mecanismos establecidos por el responsable del tratamiento.
			""";

	private ConsentPolicy() {
	}
}
