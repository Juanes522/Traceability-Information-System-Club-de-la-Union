package co.edu.unbosque.service;

import java.time.LocalDateTime;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import co.edu.unbosque.model.PersonPartner;
import co.edu.unbosque.model.RevokedToken;
import co.edu.unbosque.repository.RevokedTokenRepository;

/**
 * Lista negra de JWT revocados: lo que hace posible un cierre de sesión real.
 *
 * <p>Un JWT firmado es válido hasta que expira y no puede «apagarse» desde el servidor. Este servicio suple esa
 * carencia anotando el {@code jti} de los tokens invalidados y permitiendo consultarlo en cada petición.
 *
 * <p>Es la contrapartida del diseño sin estado: {@link co.edu.unbosque.security.SecurityConfig} declara la
 * cadena {@code STATELESS}, pero <strong>el camino de la petición sí consulta estado</strong> por culpa de este
 * servicio. Es un intercambio consciente: poder revocar una sesión vale una consulta por petición.
 *
 * @see co.edu.unbosque.model.RevokedToken
 * @see co.edu.unbosque.security.JwtAuthenticationFilter
 */
@Service
public class TokenBlacklistService {

    private final RevokedTokenRepository repository;

    public TokenBlacklistService(RevokedTokenRepository repository) {
        this.repository = repository;
    }

    /**
     * Revoca un token sin registrar a su propietario.
     *
     * @param jti        identificador del token
     * @param expiryDate expiración original del token
     */
    public void revoke(String jti, LocalDateTime expiryDate) {
        revoke(jti, expiryDate, null);
    }

    /**
     * Revoca un token, registrando opcionalmente a su propietario.
     *
     * <p>Es <strong>idempotente</strong>: revocar dos veces el mismo token no produce error ni duplicados, lo
     * que permite que el cierre de sesión se pueda reintentar sin consecuencias.
     *
     * <p>La comprobación previa a la inserción no es segura frente a concurrencia; en caso de carrera, la
     * restricción de unicidad de la columna es la que resuelve, lanzando excepción en el segundo intento.
     *
     * <p>Un {@code jti} nulo se ignora en silencio: el cierre de sesión nunca debe fallar por recibir un token
     * ilegible, y de hecho ese endpoint responde 200 en cualquier circunstancia.
     *
     * @param jti        identificador del token; si es {@code null} no se hace nada
     * @param expiryDate expiración original, que determina cuándo podrá depurarse el registro
     * @param person     propietario del token, o {@code null} si no se pudo resolver
     */
    public void revoke(String jti, LocalDateTime expiryDate, PersonPartner person) {
        if (jti == null || repository.existsByJti(jti)) {
            return;
        }
        repository.save(new RevokedToken(jti, expiryDate, person));
    }

    /**
     * Indica si un token fue revocado.
     *
     * <p><strong>Se invoca en cada petición autenticada</strong> desde
     * {@link co.edu.unbosque.security.JwtAuthenticationFilter}, sin caché alguna, de modo que es la consulta más
     * frecuente del sistema. Si el rendimiento llegara a ser un problema, este es el punto natural para
     * introducir una caché con expiración corta.
     *
     * @param jti identificador del token; {@code null} se considera no revocado
     * @return {@code true} si el token está en la lista negra
     */
    public boolean isRevoked(String jti) {
        if (jti == null) {
            return false;
        }
        return repository.existsByJti(jti);
    }

    /**
     * Elimina cada hora las revocaciones de tokens que ya caducaron por sí mismos.
     *
     * <p>Es lo que mantiene la tabla acotada y evita que la consulta de {@link #isRevoked(String)} se degrade con
     * el tiempo. Un token expirado se rechaza igualmente por su propia fecha, así que conservar su revocación no
     * aporta nada.
     *
     * <p>Al usar {@code fixedRate} y no {@code cron}, la primera ejecución ocurre una hora después del arranque:
     * un reinicio frecuente podría dejar que la tabla acumule más de lo previsto.
     */
    @Scheduled(fixedRate = 3600000)
    public void purgeExpired() {
        repository.deleteByExpiryDateBefore(LocalDateTime.now());
    }
}
