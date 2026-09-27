/**
 * Capa de negocio: dieciocho servicios que concentran las reglas del dominio.
 *
 * <h2>Agrupación funcional</h2>
 *
 * <table border="1">
 *   <caption>Servicios por responsabilidad</caption>
 *   <tr><th>Grupo</th><th>Servicios</th></tr>
 *   <tr><td>Dominio</td>
 *       <td>{@link co.edu.unbosque.service.PersonPartnerService},
 *           {@link co.edu.unbosque.service.PartnerConsumptionService},
 *           {@link co.edu.unbosque.service.AccessService}</td></tr>
 *   <tr><td>Analítica</td>
 *       <td>{@link co.edu.unbosque.service.ConsumptionMetricsService},
 *           {@link co.edu.unbosque.service.AccessMetricsService},
 *           {@link co.edu.unbosque.service.PartnerMetricsService},
 *           {@link co.edu.unbosque.service.ProductMetricsService},
 *           {@link co.edu.unbosque.service.SecurityMetricsService},
 *           {@link co.edu.unbosque.service.SnapshotService}</td></tr>
 *   <tr><td>Reportes</td>
 *       <td>{@link co.edu.unbosque.service.ReportService},
 *           {@link co.edu.unbosque.service.PdfSupport}</td></tr>
 *   <tr><td>Auditoría</td>
 *       <td>{@link co.edu.unbosque.service.AuditService} (escritura),
 *           {@link co.edu.unbosque.service.AuditQueryService} (lectura)</td></tr>
 *   <tr><td>Notificación</td>
 *       <td>{@link co.edu.unbosque.service.EmailService},
 *           {@link co.edu.unbosque.service.PushNotificationService}</td></tr>
 *   <tr><td>Integración</td>
 *       <td>{@link co.edu.unbosque.service.PartnerSyncService}</td></tr>
 *   <tr><td>Soporte de seguridad</td>
 *       <td>{@link co.edu.unbosque.service.TokenBlacklistService},
 *           {@link co.edu.unbosque.service.RateLimitService}</td></tr>
 * </table>
 *
 * <h2>Reglas de negocio que viven exclusivamente en este paquete</h2>
 *
 * <ul>
 *   <li><strong>Cierre sintético del consumo.</strong> Si el cliente no envía
 *       {@code consumptionClosing}, {@link co.edu.unbosque.service.PartnerConsumptionService}
 *       inventa {@code apertura + 20 minutos}. No hay otra definición de la duración de un
 *       consumo en el sistema.</li>
 *   <li><strong>La presencia se infiere del consumo.</strong> No hay torniquete: registrar un
 *       cargo abre un {@link co.edu.unbosque.model.Access} si el socio no tenía ninguno abierto.</li>
 *   <li><strong>Como máximo un acceso abierto por socio</strong>, invariante mantenida por
 *       {@link co.edu.unbosque.service.AccessService} mediante comprobación previa a la
 *       inserción, sin índice único que la respalde.</li>
 *   <li><strong>Total del consumo</strong> = {@code consumptionValue + iva + service + tip},
 *       tratando los nulos como cero. Esta fórmula está reimplementada de forma independiente en
 *       siete lugares del código.</li>
 * </ul>
 *
 * <h2>Convenciones del paquete, y sus consecuencias</h2>
 *
 * <p><strong>«No encontrado» se expresa como {@code null}.</strong> Varios métodos devuelven
 * {@code null} en lugar de {@code Optional} o de una colección vacía, lo que empuja comprobaciones
 * de nulidad a todos los controladores.
 *
 * <p><strong>Los fallos de notificación no propagan.</strong> Correo, Web Push y auditoría
 * capturan toda excepción y la escriben en {@code System.err}, de modo que un cargo se registra
 * aunque nadie llegue a enterarse. Es deliberado —el cargo es el dato importante— pero implica que
 * los fallos de entrega son invisibles.
 *
 * <p><strong>Dos formas de calcular métricas.</strong> La mayoría agrega en SQL con
 * {@code GROUP BY}; {@link co.edu.unbosque.service.PartnerMetricsService} es la excepción y agrega
 * en memoria tras hidratar todas las entidades del rango.
 *
 * <h2>Trabajos programados</h2>
 *
 * <p>Habilitados por {@code @EnableScheduling} en la clase de arranque:
 *
 * <ul>
 *   <li>{@link co.edu.unbosque.service.AccessService#closeOpenAccesses()} — diario a las 02:00</li>
 *   <li>{@link co.edu.unbosque.service.SnapshotService#snapshotPreviousMonth()} — día 1 a las 03:00</li>
 *   <li>{@link co.edu.unbosque.service.TokenBlacklistService#purgeExpired()} — cada hora</li>
 * </ul>
 */
package co.edu.unbosque.service;
