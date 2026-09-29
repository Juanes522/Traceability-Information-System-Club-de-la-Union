package co.edu.unbosque.security;

import co.edu.unbosque.model.PersonPartner;
import co.edu.unbosque.repository.PersonPartnerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsPasswordService;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * Puente entre {@link co.edu.unbosque.model.PersonPartner} y Spring Security.
 *
 * <p>Implementa <strong>dos</strong> contratos, y la segunda implementación es fácil de pasar por alto:
 *
 * <ul>
 *   <li>{@code UserDetailsService} — resuelve el usuario a partir de su identificación.</li>
 *   <li>{@code UserDetailsPasswordService} — permite a Spring Security <strong>reencriptar
 *       automáticamente</strong> una contraseña almacenada con un esquema obsoleto. Es la pieza que hace
 *       viable la rampa de migración desde contraseñas en texto plano.</li>
 * </ul>
 *
 * <h2>El nombre de usuario es la identificación, y eso tiene consecuencias</h2>
 *
 * <p>No se autentica por correo ni por un identificador interno, sino por la cédula. Como esa columna
 * está cifrada, la consulta solo es posible con cifrado <strong>determinista</strong>: de ahí la
 * existencia de {@link DeterministicEncryptionService} y de su conversor. Es el motivo por el que el
 * sistema tiene dos esquemas de cifrado en lugar de uno.
 *
 * <h2>Se invoca en cada petición</h2>
 *
 * <p>Como el JWT no lleva claim de rol, {@link JwtAuthenticationFilter} llama a este servicio en
 * <strong>toda petición autenticada</strong>. El efecto deseado es que revocar o degradar un rol surta
 * efecto de inmediato; el costo es una consulta a la base —con descifrado de los datos de contacto— por
 * petición.
 *
 * @see SecurityConfig#passwordEncoder()
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService, UserDetailsPasswordService {

    @Autowired
    private PersonPartnerRepository personPartnerRepository;

    /**
     * Carga el usuario correspondiente a una identificación.
     *
     * <p>La autoridad se obtiene de {@link co.edu.unbosque.model.PersonPartner#getRole()}, que normaliza
     * el valor a la forma {@code ROLE_*}. Ese método es la razón por la que las expresiones
     * {@code hasAnyRole(...)} de los controladores funcionan, y por la que una fila con rol vacío degrada
     * a {@code ROLE_PARTNER} en lugar de escalar privilegios.
     *
     * <p>Se asigna <strong>exactamente una autoridad</strong> ({@code Collections.singletonList}): el
     * modelo no admite usuarios con varios roles simultáneos.
     *
     * <p>Dos detalles delicados:
     *
     * <ul>
     *   <li>El mensaje de la excepción <strong>incorpora la identificación recibida</strong>, que es un
     *       dato personal. Spring Security la convierte en {@code BadCredentialsException} antes de que
     *       llegue al cliente, pero si alguna vez se registra en un log escribiría cédulas en claro
     *       —inconsistente con el enmascaramiento deliberado que aplica {@link PiiMasking} a los
     *       correos.</li>
     *   <li>Una contraseña almacenada nula se sustituye por cadena vacía para no romper el contrato de
     *       {@code User}. Combinado con el respaldo de texto plano del codificador, es una concurrencia de
     *       dos decisiones individualmente razonables que conviene conocer.</li>
     * </ul>
     *
     * @param identification cédula del socio, en claro; se cifra al consultar
     * @return el usuario con su única autoridad
     * @throws UsernameNotFoundException si no existe socio con esa identificación
     */
    @Override
    public UserDetails loadUserByUsername(String identification) throws UsernameNotFoundException {
        PersonPartner titular = personPartnerRepository.findByIdentification(identification)
                .orElseThrow(() -> new UsernameNotFoundException("Socio Titular no encontrado con la identificación: " + identification));

        String userPassword = titular.getPassword();
        if (userPassword == null) {
            userPassword = "";
        }

        return new User(
                identification,
                userPassword,
                Collections.singletonList(new SimpleGrantedAuthority(titular.getRole()))
        );
    }

    /**
     * Reemplaza la contraseña almacenada por su versión recodificada.
     *
     * <p><strong>No lo invoca ningún código de este proyecto:</strong> lo llama
     * {@code DaoAuthenticationProvider} por su cuenta, tras un inicio de sesión exitoso, cuando
     * {@code passwordEncoder.upgradeEncoding(...)} indica que el valor guardado usa un esquema obsoleto.
     * En la práctica eso ocurre con cada contraseña heredada en texto plano, la primera vez que su titular
     * entra al sistema.
     *
     * <p>Es por tanto el mecanismo que va vaciando la deuda de contraseñas sin hash sin intervención
     * manual. Conviene tener presente el efecto colateral: al guardar la entidad completa, <strong>también
     * cifra los datos de contacto que todavía estuvieran en claro</strong>. Ese guardado inesperado es la
     * razón por la que el script de migración de anchos de columna es obligatorio en bases preexistentes;
     * sin él, este reencriptado falla con un error de truncamiento.
     *
     * @param user usuario autenticado cuya contraseña debe actualizarse
     * @param newPassword contraseña ya codificada por el {@code PasswordEncoder}
     * @return el usuario con la contraseña actualizada y las mismas autoridades
     * @throws UsernameNotFoundException si el socio desapareció entre la autenticación y esta llamada
     */
    @Override
    public UserDetails updatePassword(UserDetails user, String newPassword) {
        PersonPartner titular = personPartnerRepository.findByIdentification(user.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Socio Titular no encontrado con la identificación: " + user.getUsername()));

        titular.setPassword(newPassword);
        personPartnerRepository.save(titular);

        return new User(user.getUsername(), newPassword, user.getAuthorities());
    }
}