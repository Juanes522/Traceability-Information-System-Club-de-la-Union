/**
 * Configuración de infraestructura y tareas de arranque.
 *
 * <ul>
 *   <li>{@link co.edu.unbosque.config.SwaggerConfig} — declara el esquema de seguridad
 *       {@code bearerAuth} para la documentación OpenAPI.</li>
 *   <li>{@link co.edu.unbosque.config.RestClientConfig} — expone el {@code RestClient.Builder} que
 *       consume {@link co.edu.unbosque.service.PartnerSyncService}.</li>
 *   <li>{@link co.edu.unbosque.config.ConsentPolicy} — texto y versión de la política de
 *       tratamiento de datos personales.</li>
 *   <li>{@link co.edu.unbosque.config.SnapshotBackfillRunner} — rellena al arrancar los snapshots
 *       mensuales que falten.</li>
 * </ul>
 *
 * <p>Nótese que dos de estas clases no son configuración en sentido estricto:
 * {@code ConsentPolicy} es un contenedor de constantes de texto legal, y
 * {@code SnapshotBackfillRunner} es una tarea de datos. Ambas están documentadas individualmente
 * porque su ubicación en este paquete tiene consecuencias.
 */
package co.edu.unbosque.config;
