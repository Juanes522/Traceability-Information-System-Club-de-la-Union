/**
 * Sistema de Información de Trazabilidad del Club de la Unión de Guayaquil.
 *
 * <p>El sistema registra los consumos de alimentos y bebidas que se cargan contra la acción
 * ({@code shareNumber}) de cada socio, notifica al socio en el momento del cargo, y ofrece
 * tableros de métricas y reportes a la gerencia y a la administración.
 *
 * <h2>Organización en capas</h2>
 *
 * <p>La dirección de dependencia es estricta y descendente; no hay dependencias inversas:
 *
 * <pre>
 * {@link co.edu.unbosque.controller} → {@link co.edu.unbosque.service} → {@link co.edu.unbosque.repository} → SQL Server
 * </pre>
 *
 * <p>Alrededor de esa columna vertebral:
 *
 * <ul>
 *   <li>{@link co.edu.unbosque.model} — entidades JPA y el documento de auditoría</li>
 *   <li>{@link co.edu.unbosque.dto} — objetos de transferencia de entrada y salida</li>
 *   <li>{@link co.edu.unbosque.security} — autenticación, autorización y cifrado</li>
 *   <li>{@link co.edu.unbosque.converter} — cifrado de datos personales en reposo</li>
 *   <li>{@link co.edu.unbosque.validation} — restricciones de validación propias</li>
 *   <li>{@link co.edu.unbosque.config} — configuración y arranque</li>
 *   <li>{@link co.edu.unbosque.exception} — traducción de excepciones a respuestas HTTP</li>
 * </ul>
 *
 * <h2>Dos almacenes de persistencia</h2>
 *
 * <p>La mayor parte del dominio vive en SQL Server a través de JPA, pero la bitácora de
 * auditoría vive en Elasticsearch: {@link co.edu.unbosque.model.AuditEvent} no es una entidad
 * JPA y no tiene repositorio, sino que se escribe y consulta con {@code ElasticsearchOperations}
 * desde {@link co.edu.unbosque.service.AuditService} y
 * {@link co.edu.unbosque.service.AuditQueryService}. No existe integridad referencial entre
 * ambos almacenes: la correspondencia es semántica, por el campo {@code username}.
 *
 * <h2>Esquema de base de datos</h2>
 *
 * <p>El esquema lo genera Hibernate con {@code spring.jpa.hibernate.ddl-auto=update}. No hay
 * Flyway ni Liquibase, de modo que <strong>las anotaciones de las entidades son la definición
 * canónica del esquema</strong>. La única migración versionada es un script SQL manual y
 * obligatorio para bases de datos preexistentes.
 *
 * <h2>Modos de despliegue</h2>
 *
 * <p>El artefacto es un WAR con {@code spring-boot-starter-tomcat} en alcance {@code provided},
 * así que admite dos puntos de entrada:
 * {@link co.edu.unbosque.TraceabilitySystemClubUnionApplication#main(String[])} con Tomcat
 * embebido, o {@link co.edu.unbosque.ServletInitializer} desplegado en un Tomcat externo.
 * Esa diferencia no es inocua: véase la documentación de la clase de arranque.
 *
 * @see co.edu.unbosque.TraceabilitySystemClubUnionApplication
 */
package co.edu.unbosque;
