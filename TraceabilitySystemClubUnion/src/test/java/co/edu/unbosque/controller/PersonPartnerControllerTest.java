package co.edu.unbosque.controller;

import co.edu.unbosque.model.PersonPartner;
import co.edu.unbosque.service.AuditQueryService;
import co.edu.unbosque.service.PartnerConsumptionService;
import co.edu.unbosque.service.PersonPartnerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PersonPartnerControllerTest {

    private PersonPartnerService partnerServ;
    private co.edu.unbosque.service.PartnerConsumptionService consumptionServ;
    private AuditQueryService auditQuery;
    private co.edu.unbosque.service.PartnerSyncService partnerSyncService;
    private PersonPartnerController controller;

    @BeforeEach
    void setUp() {
        partnerServ = mock(PersonPartnerService.class);
        consumptionServ = mock(co.edu.unbosque.service.PartnerConsumptionService.class);
        auditQuery = mock(AuditQueryService.class);
        partnerSyncService = mock(co.edu.unbosque.service.PartnerSyncService.class);
        controller = new PersonPartnerController();
        ReflectionTestUtils.setField(controller, "partnerServ", partnerServ);
        ReflectionTestUtils.setField(controller, "consumptionServ", consumptionServ);
        ReflectionTestUtils.setField(controller, "auditQuery", auditQuery);
        ReflectionTestUtils.setField(controller, "partnerSyncService", partnerSyncService);
    }

    @org.junit.jupiter.api.AfterEach
    void clearContext() {
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
    }

    @Test
    void getMyLogins_returnsPage_forAuthenticatedPartner() {
        UserDetails principal = User.withUsername("0001").password("x").authorities("ROLE_PARTNER").build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));

        co.edu.unbosque.model.AuditEvent event = new co.edu.unbosque.model.AuditEvent();
        Instant timestamp = Instant.parse("2026-08-18T10:00:00Z");
        event.setTimestamp(timestamp);
        event.setIpAddress("127.0.0.1");
        Page<co.edu.unbosque.model.AuditEvent> page = new PageImpl<>(List.of(event));
        when(auditQuery.search(eq("0001"), eq(co.edu.unbosque.model.AuditEventType.LOGIN_SUCCESS),
                isNull(), isNull(), isNull(), any())).thenReturn(page);

        ResponseEntity<?> response = controller.getMyLogins(0, 10);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Page<?> body = (Page<?>) response.getBody();
        co.edu.unbosque.dto.LoginHistoryDTO first =
                (co.edu.unbosque.dto.LoginHistoryDTO) body.getContent().get(0);
        assertEquals(timestamp, first.getTimestamp());
        assertEquals("127.0.0.1", first.getIp());
    }

    @Test
    void getMyLogins_returnsUnauthorized_whenNoAuthentication() {
        SecurityContextHolder.clearContext();
        ResponseEntity<?> response = controller.getMyLogins(0, 10);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void getMyConsumptions_returnsUnauthorized_whenNoAuthentication() {
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
        org.springframework.http.ResponseEntity<?> response = controller.getMyConsumptions(null, null, 0, 10);
        org.junit.jupiter.api.Assertions.assertEquals(
                org.springframework.http.HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void getMyConsumptions_returnsPage_forAuthenticatedPartner() {
        org.springframework.security.core.userdetails.UserDetails principal =
                org.springframework.security.core.userdetails.User.withUsername("123")
                        .password("x").authorities("ROLE_PARTNER").build();
        org.springframework.security.authentication.UsernamePasswordAuthenticationToken auth =
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                        principal, null, principal.getAuthorities());
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(auth);

        co.edu.unbosque.model.PersonPartner partner = new co.edu.unbosque.model.PersonPartner();
        org.springframework.test.util.ReflectionTestUtils.setField(partner, "personId", 7L);
        when(partnerServ.getByIdentification("123")).thenReturn(partner);
        org.springframework.data.domain.Page<co.edu.unbosque.model.PartnerConsumption> page =
                new org.springframework.data.domain.PageImpl<>(java.util.List.of(new co.edu.unbosque.model.PartnerConsumption()));
        when(consumptionServ.getByPartnerPaged(org.mockito.ArgumentMatchers.eq(7L),
                org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any())).thenReturn(page);

        org.springframework.http.ResponseEntity<?> response = controller.getMyConsumptions(null, null, 0, 10);
        org.junit.jupiter.api.Assertions.assertEquals(org.springframework.http.HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void getConsumptionsByIdentification_returnsPageForExistingPartner() {
        co.edu.unbosque.model.PersonPartner partner = new co.edu.unbosque.model.PersonPartner();
        partner.setIdentification("123");
        org.springframework.test.util.ReflectionTestUtils.setField(partner, "personId", 7L);
        when(partnerServ.getByIdentification("123")).thenReturn(partner);
        org.springframework.data.domain.Page<co.edu.unbosque.model.PartnerConsumption> page =
                new org.springframework.data.domain.PageImpl<>(java.util.List.of(new co.edu.unbosque.model.PartnerConsumption()));
        when(consumptionServ.getByPartnerPaged(org.mockito.ArgumentMatchers.eq(7L), org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any())).thenReturn(page);

        org.springframework.http.ResponseEntity<?> response =
                controller.getConsumptionsByIdentification("123", null, null, 0, 10);

        org.junit.jupiter.api.Assertions.assertEquals(org.springframework.http.HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void getConsumptionsByIdentification_rejectsRangeWiderThanThreeMonths() {
        java.time.LocalDateTime from = java.time.LocalDateTime.of(2026, 1, 1, 0, 0);
        java.time.LocalDateTime to = java.time.LocalDateTime.of(2026, 6, 1, 0, 0);
        org.springframework.http.ResponseEntity<?> response =
                controller.getConsumptionsByIdentification("123", from, to, 0, 10);
        org.junit.jupiter.api.Assertions.assertEquals(org.springframework.http.HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void getAllPaged_returnsPageAndClampsParams() {
        Page<PersonPartner> page = new PageImpl<>(List.of(new PersonPartner()));
        when(partnerServ.getAllPaged(any(Pageable.class))).thenReturn(page);

        ResponseEntity<Page<PersonPartner>> response = controller.getAllPaged(-5, 1000);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(partnerServ).getAllPaged(captor.capture());
        assertEquals(0, captor.getValue().getPageNumber());
        assertEquals(100, captor.getValue().getPageSize());
    }

    @Test
    void sync_devuelveResultado() {
        when(partnerSyncService.sync()).thenReturn(new co.edu.unbosque.dto.SyncResultDTO(880L, 0L));
        ResponseEntity<?> resp = controller.sync();
        assertThat(resp.getStatusCode().value()).isEqualTo(200);
        assertThat(((co.edu.unbosque.dto.SyncResultDTO) resp.getBody()).getCreated()).isEqualTo(880L);
    }
}
