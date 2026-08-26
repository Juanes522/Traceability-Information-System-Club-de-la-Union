package co.edu.unbosque.service;

import io.github.bucket4j.ConsumptionProbe;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RateLimitServiceTest {

    private RateLimitService service;

    @BeforeEach
    void setUp() {
        service = new RateLimitService(3, 60, 2, 1, 2, 3600);
    }

    @Test
    void loginByIp_allowsUpToCapacity_thenRejects() {
        for (int i = 0; i < 3; i++) {
            assertTrue(service.tryConsumeLoginByIp("10.0.0.1").isConsumed());
        }
        ConsumptionProbe probe = service.tryConsumeLoginByIp("10.0.0.1");
        assertFalse(probe.isConsumed());
        assertTrue(probe.getNanosToWaitForRefill() > 0);
    }

    @Test
    void loginByIp_independentKeysDoNotAffectEachOther() {
        for (int i = 0; i < 3; i++) {
            service.tryConsumeLoginByIp("10.0.0.1");
        }
        assertFalse(service.tryConsumeLoginByIp("10.0.0.1").isConsumed());
        assertTrue(service.tryConsumeLoginByIp("10.0.0.2").isConsumed());
    }

    @Test
    void forgotPassword_hasItsOwnBucket() {
        assertTrue(service.tryConsumeForgotPassword("10.0.0.1").isConsumed());
        assertTrue(service.tryConsumeForgotPassword("10.0.0.1").isConsumed());
        assertFalse(service.tryConsumeForgotPassword("10.0.0.1").isConsumed());
        assertTrue(service.tryConsumeLoginByIp("10.0.0.1").isConsumed());
    }

    @Test
    void user_notBlockedInitially() {
        assertFalse(service.isUserBlocked("user1"));
    }

    @Test
    void user_blockedAfterCapacityFailures() {
        service.registerFailedLogin("user1");
        assertFalse(service.isUserBlocked("user1"));
        service.registerFailedLogin("user1");
        assertTrue(service.isUserBlocked("user1"));
    }

    @Test
    void user_unblockedAfterReset() {
        service.registerFailedLogin("user1");
        service.registerFailedLogin("user1");
        assertTrue(service.isUserBlocked("user1"));
        service.resetUserFailures("user1");
        assertFalse(service.isUserBlocked("user1"));
    }

    @Test
    void user_unblocksAfterWindowElapses() throws InterruptedException {
        service.registerFailedLogin("user1");
        service.registerFailedLogin("user1");
        assertTrue(service.isUserBlocked("user1"));
        Thread.sleep(600);
        assertFalse(service.isUserBlocked("user1"));
    }

    @Test
    void user_nullOrBlankUsername_isNeverBlockedAndDoesNotThrow() {
        assertFalse(service.isUserBlocked(null));
        assertFalse(service.isUserBlocked(" "));
        service.registerFailedLogin(null);
        service.registerFailedLogin(" ");
        service.resetUserFailures(null);
    }

    @Test
    void user_blockedDoesNotAffectOtherUsers() {
        service.registerFailedLogin("user1");
        service.registerFailedLogin("user1");
        assertTrue(service.isUserBlocked("user1"));
        assertFalse(service.isUserBlocked("user2"));
    }
}
