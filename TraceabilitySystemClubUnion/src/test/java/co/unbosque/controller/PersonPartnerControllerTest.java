package co.unbosque.controller;

import co.unbosque.model.PersonPartner;
import co.unbosque.service.PartnerConsumptionService;
import co.unbosque.service.PersonPartnerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PersonPartnerControllerTest {

    private PersonPartnerService partnerServ;
    private co.unbosque.service.PartnerConsumptionService consumptionServ;
    private PersonPartnerController controller;

    @BeforeEach
    void setUp() {
        partnerServ = mock(PersonPartnerService.class);
        consumptionServ = mock(co.unbosque.service.PartnerConsumptionService.class);
        controller = new PersonPartnerController();
        ReflectionTestUtils.setField(controller, "partnerServ", partnerServ);
        ReflectionTestUtils.setField(controller, "consumptionServ", consumptionServ);
    }

    @org.junit.jupiter.api.AfterEach
    void clearContext() {
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
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

        co.unbosque.model.PersonPartner partner = new co.unbosque.model.PersonPartner();
        org.springframework.test.util.ReflectionTestUtils.setField(partner, "personId", 7L);
        when(partnerServ.getByIdentification("123")).thenReturn(partner);
        org.springframework.data.domain.Page<co.unbosque.model.PartnerConsumption> page =
                new org.springframework.data.domain.PageImpl<>(java.util.List.of(new co.unbosque.model.PartnerConsumption()));
        when(consumptionServ.getByPartnerPaged(org.mockito.ArgumentMatchers.eq(7L),
                org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any())).thenReturn(page);

        org.springframework.http.ResponseEntity<?> response = controller.getMyConsumptions(null, null, 0, 10);
        org.junit.jupiter.api.Assertions.assertEquals(org.springframework.http.HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void getConsumptionsByIdentification_returnsPageForExistingPartner() {
        co.unbosque.model.PersonPartner partner = new co.unbosque.model.PersonPartner();
        partner.setIdentification("123");
        org.springframework.test.util.ReflectionTestUtils.setField(partner, "personId", 7L);
        when(partnerServ.getByIdentification("123")).thenReturn(partner);
        org.springframework.data.domain.Page<co.unbosque.model.PartnerConsumption> page =
                new org.springframework.data.domain.PageImpl<>(java.util.List.of(new co.unbosque.model.PartnerConsumption()));
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
}
