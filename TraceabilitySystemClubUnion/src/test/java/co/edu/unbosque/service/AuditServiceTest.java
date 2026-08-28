package co.edu.unbosque.service;

import co.edu.unbosque.model.AuditEvent;
import co.edu.unbosque.model.AuditEventType;
import co.edu.unbosque.model.AuditResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuditServiceTest {

    private ElasticsearchOperations operations;
    private AuditService service;

    @BeforeEach
    void setUp() {
        operations = mock(ElasticsearchOperations.class);
        service = new AuditService(operations);
    }

    @Test
    void record_buildsAndSavesEventWithAllFields() {
        service.record(AuditEventType.LOGIN_FAILED, AuditResult.FAILURE,
                "123", "10.0.0.1", "Credenciales incorrectas", null);

        ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);
        verify(operations).save(captor.capture());
        AuditEvent saved = captor.getValue();
        assertEquals(AuditEventType.LOGIN_FAILED, saved.getEventType());
        assertEquals(AuditResult.FAILURE, saved.getResult());
        assertEquals("123", saved.getUsername());
        assertEquals("10.0.0.1", saved.getIpAddress());
        assertEquals("Credenciales incorrectas", saved.getDetail());
        assertNotNull(saved.getTimestamp());
    }

    @Test
    void record_setsCriticalSeverity_forRateLimitBlock() {
        service.record(AuditEventType.RATE_LIMIT_BLOCK, AuditResult.FAILURE, "123", "10.0.0.1", "bloqueo", null);
        ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);
        verify(operations).save(captor.capture());
        assertEquals(co.edu.unbosque.model.AuditSeverity.CRITICAL, captor.getValue().getSeverity());
    }

    @Test
    void record_setsCriticalSeverity_forAccessDenied() {
        service.record(AuditEventType.ACCESS_DENIED, AuditResult.FAILURE, "123", "10.0.0.1", "denegado", null);
        ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);
        verify(operations).save(captor.capture());
        assertEquals(co.edu.unbosque.model.AuditSeverity.CRITICAL, captor.getValue().getSeverity());
    }

    @Test
    void record_setsWarningSeverity_forFailureResult() {
        service.record(AuditEventType.LOGIN_FAILED, AuditResult.FAILURE, "123", "10.0.0.1", "fallo", null);
        ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);
        verify(operations).save(captor.capture());
        assertEquals(co.edu.unbosque.model.AuditSeverity.WARNING, captor.getValue().getSeverity());
    }

    @Test
    void record_setsInfoSeverity_forSuccessEvent() {
        service.record(AuditEventType.LOGIN_SUCCESS, AuditResult.SUCCESS, "123", "10.0.0.1", "ok", null);
        ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);
        verify(operations).save(captor.capture());
        assertEquals(co.edu.unbosque.model.AuditSeverity.INFO, captor.getValue().getSeverity());
    }

    @Test
    void record_usesProvidedTimestamp() {
        java.time.Instant ts = java.time.Instant.parse("2026-05-15T16:45:00Z");
        service.record(AuditEventType.CHARGE_REGISTERED, AuditResult.SUCCESS,
                "123", "10.0.0.1", "cargo", "9", ts);

        ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);
        verify(operations).save(captor.capture());
        assertEquals(ts, captor.getValue().getTimestamp());
    }

    @Test
    void record_isFailOpen_whenSaveThrows() {
        when(operations.save(any(AuditEvent.class))).thenThrow(new RuntimeException("ES down"));

        assertDoesNotThrow(() -> service.record(AuditEventType.LOGIN_SUCCESS,
                AuditResult.SUCCESS, "123", "10.0.0.1", "ok", null));
    }
}
