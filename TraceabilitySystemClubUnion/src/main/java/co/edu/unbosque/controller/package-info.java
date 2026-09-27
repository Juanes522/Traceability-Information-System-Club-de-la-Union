/**
 * Capa web: once controladores REST que exponen 48 endpoints.
 *
 * <table border="1">
 *   <caption>Controladores y prefijo de ruta</caption>
 *   <tr><th>Controlador</th><th>Prefijo</th><th>Responsabilidad</th></tr>
 *   <tr><td>{@link co.edu.unbosque.controller.AuthController}</td><td>{@code /auth}</td>
 *       <td>Inicio y cierre de sesión, contraseñas, consentimiento</td></tr>
 *   <tr><td>{@link co.edu.unbosque.controller.PersonPartnerController}</td><td>{@code /personpartner}</td>
 *       <td>Consulta de socios, datos propios, sincronización</td></tr>
 *   <tr><td>{@link co.edu.unbosque.controller.PartnerConsumptionController}</td><td>{@code /partnerconsumption}</td>
 *       <td>Registro y consulta de cargos</td></tr>
 *   <tr><td>{@link co.edu.unbosque.controller.MetricsController}</td><td>{@code /metrics}</td>
 *       <td>Métricas de consumo y de seguridad</td></tr>
 *   <tr><td>{@link co.edu.unbosque.controller.AccessMetricsController}</td><td>{@code /metrics/access}</td>
 *       <td>Afluencia y ocupación</td></tr>
 *   <tr><td>{@link co.edu.unbosque.controller.PartnerMetricsController}</td><td>{@code /metrics/partner}</td>
 *       <td>Métricas individuales</td></tr>
 *   <tr><td>{@link co.edu.unbosque.controller.ProductMetricsController}</td><td>{@code /metrics/products}</td>
 *       <td>Analítica de productos</td></tr>
 *   <tr><td>{@link co.edu.unbosque.controller.SnapshotController}</td><td>{@code /metrics/snapshots}</td>
 *       <td>Serie mensual precalculada</td></tr>
 *   <tr><td>{@link co.edu.unbosque.controller.ReportController}</td><td>{@code /reports}</td>
 *       <td>Generación de PDF</td></tr>
 *   <tr><td>{@link co.edu.unbosque.controller.AuditController}</td><td>{@code /audit}</td>
 *       <td>Consulta de la bitácora</td></tr>
 *   <tr><td>{@link co.edu.unbosque.controller.PushSubscriptionController}</td><td>{@code /push}</td>
 *       <td>Suscripciones Web Push</td></tr>
 * </table>
 *
 * <h2>Los dos patrones de acceso a datos propios</h2>
 *
 * <p><strong>Patrón {@code /me}</strong>, el dominante y estructuralmente seguro: la identidad se
 * resuelve del {@code SecurityContextHolder} y <strong>el cliente nunca envía un
 * identificador</strong>. Lo usan {@code /personpartner/me}, {@code /getconsumptions/me},
 * {@code /notifications/me}, {@code /my-logins}, {@code /metrics/partner/me} y
 * {@code /metrics/products/partner/me}.
 *
 * <p><strong>Guarda explícita</strong>, un único caso: {@code canAccessPartner} en
 * {@link co.edu.unbosque.controller.PartnerConsumptionController}, que permite el acceso a los
 * roles privilegiados y, al resto, solo a sus propios datos.
 *
 * <h2>Advertencia sobre la aparente permisividad de {@code /auth}</h2>
 *
 * <p>{@link co.edu.unbosque.security.SecurityConfig} declara {@code /auth/**} como
 * {@code permitAll()}, lo que incluye {@code /auth/change-password}, {@code /auth/logout} y
 * {@code /auth/accept-consent}. <strong>No son operaciones anónimas:</strong>
 * {@link co.edu.unbosque.controller.AuthController} comprueba el contexto de seguridad a mano en
 * cada una. El efecto es correcto, pero quien lea solo la configuración concluirá lo contrario.
 *
 * <h2>Inconsistencias transversales</h2>
 *
 * <p><strong>Dos políticas de rango temporal.</strong> Los controladores de métricas y reportes
 * definen {@code MAX_RANGE_DAYS = 366} con una ventana por defecto de 30 días —en cinco copias
 * casi idénticas de {@code resolve()} y {@code rangeError()}—, mientras que
 * {@link co.edu.unbosque.controller.PersonPartnerController},
 * {@link co.edu.unbosque.controller.PartnerConsumptionController} y
 * {@link co.edu.unbosque.controller.AuditController} aplican un tope de 92 días insertado en línea
 * y no establecen ventana por defecto.
 *
 * <p><strong>Se serializan entidades, no DTO de salida.</strong> Los endpoints de socios y de
 * consumos devuelven las entidades JPA directamente. Véase
 * {@link co.edu.unbosque.model.PersonPartner} para las consecuencias.
 *
 * <p><strong>{@link co.edu.unbosque.controller.PushSubscriptionController} rompe la capa:</strong>
 * inyecta un repositorio y persiste directamente, sin pasar por un servicio. Es el único que lo
 * hace.
 *
 * @see co.edu.unbosque.exception.GlobalExceptionHandler
 */
package co.edu.unbosque.controller;
