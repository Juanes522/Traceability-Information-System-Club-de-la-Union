/**
 * Conversores de atributos JPA que cifran datos personales en reposo.
 *
 * <p>Tres conversores cifran, y uno ({@link co.edu.unbosque.converter.StringArrayConverter}) es su
 * predecesor sin cifrado, conservado tras la migración y hoy sin referencias.
 *
 * <p>Todos se declaran <strong>sin {@code autoApply = true}</strong>, de modo que se aplican
 * explícitamente campo por campo con {@code @Convert}. La única entidad que los usa es
 * {@link co.edu.unbosque.model.PersonPartner}:
 *
 * <table border="1">
 *   <caption>Correspondencia entre campo y conversor</caption>
 *   <tr><th>Campo</th><th>Conversor</th><th>Prefijo</th></tr>
 *   <tr><td>{@code identification}</td>
 *       <td>{@link co.edu.unbosque.converter.DeterministicEncryptedStringConverter}</td>
 *       <td>{@code DET:v1:}</td></tr>
 *   <tr><td>{@code phone}, {@code cellPhone}</td>
 *       <td>{@link co.edu.unbosque.converter.EncryptedStringConverter}</td>
 *       <td>{@code ENC:v1:}</td></tr>
 *   <tr><td>{@code email} ({@code String[]})</td>
 *       <td>{@link co.edu.unbosque.converter.EncryptedStringArrayConverter}</td>
 *       <td>{@code ENC:v1:}</td></tr>
 * </table>
 *
 * <h2>Por qué hay dos esquemas de cifrado</h2>
 *
 * <p>Es la decisión de diseño más importante del paquete.
 * {@link co.edu.unbosque.security.AesGcmEncryptionService} usa un IV aleatorio por operación —lo
 * correcto para cifrado autenticado—, pero eso vuelve imposibles las búsquedas por igualdad, los
 * índices y las restricciones de unicidad sobre esas columnas.
 *
 * <p>El campo {@code identification} <strong>no puede permitirse eso</strong>: es el nombre de
 * usuario de Spring Security, y
 * {@link co.edu.unbosque.security.UserDetailsServiceImpl#loadUserByUsername(String)} necesita
 * resolverlo con una comparación de igualdad. De ahí la existencia de
 * {@link co.edu.unbosque.security.DeterministicEncryptionService}, que deriva el IV del propio
 * texto en claro. El precio asumido es que el cifrado determinista revela igualdad.
 *
 * <h2>Migración progresiva</h2>
 *
 * <p>Ningún conversor de este paquete contiene lógica de migración: la tienen los servicios de
 * cifrado, en una sola línea de sus métodos {@code decrypt} que devuelve sin modificar cualquier
 * valor que no lleve el prefijo esperado. Las filas anteriores a la migración siguen siendo
 * legibles y se cifran en el siguiente guardado de la entidad; como la escritura siempre cifra,
 * el texto en claro no puede reaparecer.
 *
 * <h2>Dependencia invisible del contenedor</h2>
 *
 * <p>Los tres conversores que cifran reciben su servicio por constructor, de modo que
 * <strong>Hibernate no puede instanciarlos por reflexión</strong>. Funcionan porque Spring Boot
 * conecta su {@code SpringBeanContainer} con Hibernate. Consecuencia práctica: una prueba con
 * {@code @DataJpaTest} debe importar los servicios de cifrado explícitamente, como hace
 * {@code PersonPartnerIdentificationEncryptionTest}.
 */
package co.edu.unbosque.converter;
