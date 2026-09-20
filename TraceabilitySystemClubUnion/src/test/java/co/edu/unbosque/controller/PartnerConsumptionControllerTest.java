package co.edu.unbosque.controller;

import co.edu.unbosque.model.PartnerConsumption;
import co.edu.unbosque.model.PersonPartner;
import co.edu.unbosque.service.PartnerConsumptionService;
import co.edu.unbosque.service.PersonPartnerService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class PartnerConsumptionControllerTest {

    private PartnerConsumptionService consumptionServ;
    private PersonPartnerService personPartnerService;
    private PartnerConsumptionController controller;

    @BeforeEach
    void setUp() {
        consumptionServ = mock(PartnerConsumptionService.class);
        personPartnerService = mock(PersonPartnerService.class);
        controller = new PartnerConsumptionController();
        ReflectionTestUtils.setField(controller, "consumptionServ", consumptionServ);
        ReflectionTestUtils.setField(controller, "personPartnerService", personPartnerService);
    }

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    private void authenticateAs(String username, String role) {
        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(role));
        User principal = new User(username, "", authorities);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, authorities));
    }

    @Test
    void getByPartner_partnerCannotAccessAnotherPartnersConsumptions() {
        PersonPartner me = new PersonPartner();
        me.setPersonId(1L);
        when(personPartnerService.getByIdentification("100")).thenReturn(me);
        authenticateAs("100", "ROLE_PARTNER");

        ResponseEntity<List<PartnerConsumption>> response = controller.getByPartner(2L);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        verify(consumptionServ, never()).getByPartnerId(any());
    }

    @Test
    void getByPartner_partnerCanAccessOwnConsumptions() {
        PersonPartner me = new PersonPartner();
        me.setPersonId(1L);
        when(personPartnerService.getByIdentification("100")).thenReturn(me);
        when(consumptionServ.getByPartnerId(1L)).thenReturn(List.of(new PartnerConsumption()));
        authenticateAs("100", "ROLE_PARTNER");

        ResponseEntity<List<PartnerConsumption>> response = controller.getByPartner(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void getByPartner_managerCanAccessAnyPartnersConsumptions() {
        when(consumptionServ.getByPartnerId(2L)).thenReturn(List.of(new PartnerConsumption()));
        authenticateAs("manager", "ROLE_MANAGER");

        ResponseEntity<List<PartnerConsumption>> response = controller.getByPartner(2L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verifyNoInteractions(personPartnerService);
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
