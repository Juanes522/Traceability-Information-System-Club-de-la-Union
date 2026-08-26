package co.edu.unbosque.controller;

import co.edu.unbosque.model.AuditEvent;
import co.edu.unbosque.service.AuditQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class AuditControllerTest {

    private AuditQueryService queryService;
    private AuditController controller;

    @BeforeEach
    void setUp() {
        queryService = mock(AuditQueryService.class);
        controller = new AuditController(queryService);
    }

    @Test
    void search_passesFiltersAndPaginationToService_andReturnsOkPage() {
        Page<AuditEvent> page = new PageImpl<>(List.of(new AuditEvent()));
        when(queryService.search(eq("123"), eq("LOGIN_FAILED"), eq("FAILURE"),
                any(), any(), any(Pageable.class))).thenReturn(page);

        Instant from = Instant.parse("2026-08-01T00:00:00Z");
        Instant to = Instant.parse("2026-08-12T00:00:00Z");
        ResponseEntity<?> response =
                controller.search("123", "LOGIN_FAILED", "FAILURE", from, to, 0, 20);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, ((Page<?>) response.getBody()).getTotalElements());

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(queryService).search(eq("123"), eq("LOGIN_FAILED"), eq("FAILURE"),
                eq(from), eq(to), pageableCaptor.capture());
        assertEquals(0, pageableCaptor.getValue().getPageNumber());
        assertEquals(20, pageableCaptor.getValue().getPageSize());
    }

    @Test
    void search_clampsNegativePageAndOversizeSize() {
        when(queryService.search(any(), any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        controller.search(null, null, null, null, null, -5, 1000);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(queryService).search(any(), any(), any(), any(), any(), pageableCaptor.capture());
        assertEquals(0, pageableCaptor.getValue().getPageNumber());
        assertEquals(100, pageableCaptor.getValue().getPageSize());
    }

    @Test
    void search_rejectsRangeWiderThanThreeMonths() {
        Instant from = Instant.parse("2026-01-01T00:00:00Z");
        Instant to = Instant.parse("2026-06-01T00:00:00Z");

        ResponseEntity<?> response = controller.search(null, null, null, from, to, 0, 20);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verifyNoInteractions(queryService);
    }
}
