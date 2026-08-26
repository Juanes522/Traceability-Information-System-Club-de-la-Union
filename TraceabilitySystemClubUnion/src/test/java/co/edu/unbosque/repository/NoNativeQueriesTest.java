package co.edu.unbosque.repository;

import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.Query;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;

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
