package co.edu.unbosque.controller;

import co.edu.unbosque.model.PartnerConsumption;
import co.edu.unbosque.service.PartnerConsumptionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class PartnerConsumptionControllerTest {

    private PartnerConsumptionService consumptionServ;
    private PartnerConsumptionController controller;

    @BeforeEach
    void setUp() {
        consumptionServ = mock(PartnerConsumptionService.class);
        controller = new PartnerConsumptionController();
        ReflectionTestUtils.setField(controller, "consumptionServ", consumptionServ);
    }

    @Test
    void getByEnvironment_returnsPageAndClampsParams() {
        Page<PartnerConsumption> page = new PageImpl<>(List.of(new PartnerConsumption()));
        when(consumptionServ.getByEnviromentPaged(eq("Bar"), any(), any(), any(Pageable.class))).thenReturn(page);

        ResponseEntity<?> response = controller.getByEnvironment("Bar", null, null, -5, 1000);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(consumptionServ).getByEnviromentPaged(eq("Bar"), any(), any(), captor.capture());
        assertEquals(0, captor.getValue().getPageNumber());
        assertEquals(100, captor.getValue().getPageSize());
    }

    @Test
    void getByEnvironment_rejectsRangeWiderThanThreeMonths() {
        LocalDateTime from = LocalDateTime.of(2026, 1, 1, 0, 0);
        LocalDateTime to = LocalDateTime.of(2026, 6, 1, 0, 0);

        ResponseEntity<?> response = controller.getByEnvironment("Bar", from, to, 0, 10);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verifyNoInteractions(consumptionServ);
    }
}
