/**
 * Repositorios de Spring Data JPA: la capa de acceso a datos relacionales.
 *
 * <p>Nueve interfaces, todas extendiendo {@code JpaRepository}. No hay implementaciones propias ni
 * consultas nativas —{@code NoNativeQueriesTest} verifica esto último como prueba de
 * arquitectura, lo que mantiene la portabilidad de dialecto.
 *
 * <p><strong>No existe repositorio para {@link co.edu.unbosque.model.AuditEvent}</strong>: la
 * bitácora vive en Elasticsearch y se accede con {@code ElasticsearchOperations} desde
 * {@link co.edu.unbosque.service.AuditService} y {@link co.edu.unbosque.service.AuditQueryService}.
 *
 * <h2>Estilos de consulta presentes</h2>
 *
 * <ul>
 *   <li><strong>Derivadas del nombre</strong> — la mayoría. El recorrido de asociaciones se
 *       expresa concatenando nombres de propiedad, como en
 *       {@code findByConsumptionPartnerIdentificationOrderByGenerationDateDesc}, que atraviesa
 *       tres niveles.</li>
 *   <li><strong>{@code @Query} con JPQL</strong> — para agregaciones. Devuelven
 *       {@code List<Object[]>} posicional, cuyo significado por índice vive únicamente en el
 *       servicio que las consume.</li>
 *   <li><strong>Proyección por interfaz</strong> — un solo caso,
 *       {@link co.edu.unbosque.dto.ConsumptionRowView}, usado por
 *       {@code findRowsInRange} para evitar hidratar entidades completas.</li>
 *   <li><strong>{@code @Modifying}</strong> — dos operaciones masivas de escritura.</li>
 * </ul>
 *
 * <h2>Consideraciones transversales</h2>
 *
 * <p><strong>Columnas cifradas.</strong> Las consultas que filtran por {@code identification}
 * funcionan porque el parámetro atraviesa
 * {@link co.edu.unbosque.converter.DeterministicEncryptedStringConverter} antes de llegar al
 * {@code WHERE}, comparando criptograma contra criptograma. Por el contrario, <strong>no existe
 * ningún {@code findByEmail}</strong>: ese campo usa cifrado con IV aleatorio y no es consultable
 * (véase {@link co.edu.unbosque.service.PersonPartnerService#getByEmail(String)}).
 *
 * <p><strong>Uniones implícitas.</strong> Toda consulta JPQL que referencie {@code c.partner.personId}
 * genera un <em>inner join</em> implícito y por tanto <strong>excluye</strong> las filas cuya clave
 * ajena es nula. Como {@code partner_consumption.person_id} admite nulos, esas consultas pueden
 * discrepar de las que solo agregan importes sin mencionar al socio.
 *
 * <p><strong>Anotación {@code @Repository} desigual.</strong> Está presente en seis de las nueve
 * interfaces y ausente en tres. No tiene efecto funcional —Spring Data registra los proxies de
 * todos modos—, pero la inconsistencia es real.
 */
package co.edu.unbosque.repository;
