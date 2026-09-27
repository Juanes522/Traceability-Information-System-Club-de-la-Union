/**
 * Entidades del dominio y el documento de auditoría.
 *
 * <p>Diez entidades JPA persisten en SQL Server; {@link co.edu.unbosque.model.AuditEvent} es la
 * excepción: es un documento de Spring Data Elasticsearch, no una entidad relacional.
 *
 * <h2>Entidad central</h2>
 *
 * <p>{@link co.edu.unbosque.model.PersonPartner} representa indistintamente al socio y a los
 * usuarios de gestión: <strong>no hay tabla de usuarios separada</strong>, el rol es una columna
 * de esa misma tabla. Es también la única entidad con campos cifrados.
 *
 * <h2>Hecho de negocio</h2>
 *
 * <p>{@link co.edu.unbosque.model.PartnerConsumption} es el cargo contra la acción del socio, con
 * sus líneas de detalle ({@link co.edu.unbosque.model.ConsumptionItem}) y los avisos que genera
 * ({@link co.edu.unbosque.model.Notification}).
 *
 * <h2>Convenciones del paquete</h2>
 *
 * <ul>
 *   <li>Toda clave primaria usa {@code GenerationType.IDENTITY}.</li>
 *   <li>Todas las asociaciones son {@code LAZY}, y el lado propietario de la clave ajena es
 *       siempre el hijo ({@code @ManyToOne}).</li>
 *   <li>Las colecciones inversas llevan {@code @JsonIgnore} para no serializar el grafo
 *       completo, con {@code cascade = ALL} y {@code orphanRemoval = true}: eliminar un padre
 *       elimina su historial.</li>
 *   <li>No hay callbacks de ciclo de vida ({@code @PrePersist}/{@code @PreUpdate}), ni
 *       {@code @Version}, ni índices declarados a nivel de tabla.</li>
 * </ul>
 *
 * <h2>Advertencia sobre la frontera HTTP</h2>
 *
 * <p>Varias entidades de este paquete se serializan <strong>directamente</strong> como respuesta
 * HTTP, sin DTO de salida. Eso significa que cualquier campo que se añada aquí pasa a ser API
 * pública de forma automática. Véase {@link co.edu.unbosque.model.PersonPartner} para las
 * consecuencias concretas.
 *
 * <h2>Lo que este paquete NO modela</h2>
 *
 * <p>No existe jerarquía titular/dependiente: {@code PersonPartner} no tiene relación reflexiva
 * ni columna {@code owner_id}. Tampoco existe ninguna entidad de validación de consumo: la
 * notificación al socio es de un solo sentido, sin confirmación ni rechazo.
 */
package co.edu.unbosque.model;
