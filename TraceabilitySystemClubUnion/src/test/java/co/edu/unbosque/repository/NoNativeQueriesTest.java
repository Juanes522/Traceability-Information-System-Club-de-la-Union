package co.edu.unbosque.repository;

import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.Query;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Prueba de arquitectura: verifica que ningún repositorio declara consultas nativas.
 *
 * <p>No comprueba comportamiento sino una restricción de diseño. Su valor es preventivo: mantener todas las consultas en el
 * lenguaje de consulta de la capa de persistencia conserva la portabilidad de dialecto, algo especialmente relevante aquí,
 * donde las pruebas corren sobre una base en memoria y producción sobre SQL Server.
 */
class NoNativeQueriesTest {

    private static final List<Class<?>> REPOSITORIES = List.of(
            AccessRepository.class,
            NotificationRepository.class,
            PartnerConsumptionRepository.class,
            PasswordResetTokenRepository.class,
            PushSubscriptionRepository.class,
            PersonPartnerRepository.class);

    @Test
    void noRepositoryDeclaresNativeQuery() {
        for (Class<?> repository : REPOSITORIES) {
            for (Method method : repository.getMethods()) {
                Query query = method.getAnnotation(Query.class);
                boolean isNative = query != null && query.nativeQuery();
                assertFalse(isNative,
                        repository.getSimpleName() + "." + method.getName()
                                + " usa nativeQuery=true; revisar contra SQL injection antes de permitirlo");
            }
        }
    }
}
