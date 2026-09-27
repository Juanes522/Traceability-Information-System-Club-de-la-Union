/**
 * Objetos de transferencia de datos.
 *
 * <p>Treinta y tres tipos. Todos son JavaBeans mutables con constructor sin argumentos y, en la
 * mayoría de casos, un constructor con todos los campos; ninguno es un {@code record}.
 *
 * <h2>Agrupación por función</h2>
 *
 * <table border="1">
 *   <caption>DTO por subsistema</caption>
 *   <tr><th>Subsistema</th><th>Entrada</th><th>Salida</th></tr>
 *   <tr><td>Autenticación</td>
 *       <td>{@link co.edu.unbosque.dto.AuthRequest},
 *           {@link co.edu.unbosque.dto.ChangePasswordRequest},
 *           {@link co.edu.unbosque.dto.ForgotPasswordRequest},
 *           {@link co.edu.unbosque.dto.ResetPasswordRequest}</td>
 *       <td>{@link co.edu.unbosque.dto.AuthResponse},
 *           {@link co.edu.unbosque.dto.ConsentPolicyDTO},
 *           {@link co.edu.unbosque.dto.LoginHistoryDTO}</td></tr>
 *   <tr><td>Consumos</td>
 *       <td>{@link co.edu.unbosque.dto.ConsumptionCreateRequest},
 *           {@link co.edu.unbosque.dto.ConsumptionItemRequest}</td>
 *       <td>{@link co.edu.unbosque.dto.NotificationDTO},
 *           {@link co.edu.unbosque.dto.ConsumptionRowView} (proyección interna)</td></tr>
 *   <tr><td>Métricas</td><td>—</td>
 *       <td>{@code ConsumptionSummaryDTO}, {@code EnvironmentTotalDTO}, {@code TrendPointDTO},
 *           {@code ComparisonDTO}, {@code PeakDTO}, {@code HourBucketDTO},
 *           {@code WeekdayBucketDTO}, {@code PeakHeatmapCellDTO}, {@code AccessSummaryDTO},
 *           {@code AttendancePointDTO}, {@code EnvironmentOccupancyDTO},
 *           {@code PartnerMetricsDTO}, {@code ProductRankDTO}, {@code CategoryMixDTO},
 *           {@code ProductDetailDTO}, {@code EnvironmentCategoryDTO},
 *           {@code MonthlySnapshotDTO}</td></tr>
 *   <tr><td>Seguridad</td><td>—</td>
 *       <td>{@link co.edu.unbosque.dto.SecuritySummaryDTO},
 *           {@link co.edu.unbosque.dto.UserFailedCountDTO}</td></tr>
 *   <tr><td>Web Push</td>
 *       <td>{@link co.edu.unbosque.dto.PushSubscriptionRequest}</td><td>—</td></tr>
 *   <tr><td>Sincronización</td>
 *       <td>{@link co.edu.unbosque.dto.ExternalSocioDTO}</td>
 *       <td>{@link co.edu.unbosque.dto.SyncResultDTO}</td></tr>
 * </table>
 *
 * <p>Los reportes no tienen DTO: {@link co.edu.unbosque.controller.ReportController} devuelve
 * {@code byte[]} con el PDF ya generado.
 *
 * <h2>Cobertura desigual del patrón</h2>
 *
 * <p>Los DTO de entrada están completos y bien validados, pero <strong>faltan DTO de salida para
 * el dominio</strong>: los endpoints de socios y de consumos serializan las entidades JPA
 * directamente. Las consecuencias están documentadas en
 * {@link co.edu.unbosque.model.PersonPartner}. Lo mismo ocurre con la bitácora:
 * {@code GET /audit} devuelve {@link co.edu.unbosque.model.AuditEvent} sin frontera de DTO.
 *
 * <h2>Validación</h2>
 *
 * <p>Se aplica con Jakarta Bean Validation y {@code @Valid} en el controlador; los incumplimientos
 * los traduce {@link co.edu.unbosque.exception.GlobalExceptionHandler}. La restricción propia
 * {@link co.edu.unbosque.validation.StrongPassword} protege los dos DTO que fijan una contraseña
 * nueva, pero no el de inicio de sesión —correctamente, para que las contraseñas heredadas sigan
 * funcionando hasta que se fuerce el cambio.
 *
 * <p><strong>No hay validación cruzada de campos en ningún DTO.</strong> Nada comprueba que el
 * cierre de un consumo sea posterior a su apertura, ni que la suma de sus líneas coincida con el
 * valor declarado.
 *
 * <h2>Nota sobre la ortografía de «ambiente»</h2>
 *
 * <p>{@link co.edu.unbosque.dto.ConsumptionCreateRequest} usa {@code enviroment} (sin la segunda
 * «n») para reflejar el campo de la entidad, mientras que los DTO de métricas usan la forma
 * correcta {@code environment}. Ambas grafías son intencionales en su contexto y no deben
 * unificarse sin cambiar también las consultas JPQL y el modelo del frontend.
 */
package co.edu.unbosque.dto;
