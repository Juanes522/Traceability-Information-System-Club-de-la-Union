package co.edu.unbosque.service;

import co.edu.unbosque.model.PartnerConsumption;
import co.edu.unbosque.model.PersonPartner;
import co.edu.unbosque.repository.PersonPartnerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PersonPartnerServiceTest {

    private PersonPartnerRepository repository;
    private PersonPartnerService service;

    @BeforeEach
    void setUp() {
        repository = mock(PersonPartnerRepository.class);
        service = new PersonPartnerService();
        ReflectionTestUtils.setField(service, "partnerRepo", repository);
    }

    private PersonPartner partner(String identification) {
        PersonPartner p = new PersonPartner();
        p.setIdentification(identification);
        return p;
    }

    @Test
    void getById_returnsPartnerWhenPresent_nullWhenAbsent() {
        PersonPartner p = partner("100");
        when(repository.findByPersonId(1L)).thenReturn(Optional.of(p));
        when(repository.findByPersonId(2L)).thenReturn(Optional.empty());

        assertSame(p, service.getById(1L));
        assertNull(service.getById(2L));
    }

    @Test
    void getByIdentification_returnsPartnerWhenPresent_nullWhenAbsent() {
        PersonPartner p = partner("123");
        when(repository.findByIdentification("123")).thenReturn(Optional.of(p));
        when(repository.findByIdentification("999")).thenReturn(Optional.empty());

        assertSame(p, service.getByIdentification("123"));
        assertNull(service.getByIdentification("999"));
    }

    @Test
    void getAll_delegatesToRepository() {
        List<PersonPartner> all = List.of(partner("1"), partner("2"));
        when(repository.findAll()).thenReturn(all);

        assertEquals(all, service.getAll());
    }

    @Test
    void getByFirstName_returnsListWhenFound_nullWhenEmpty() {
        List<PersonPartner> found = List.of(partner("1"));
        when(repository.findByFirstName("Ana")).thenReturn(found);
        when(repository.findByFirstName("Nadie")).thenReturn(List.of());

        assertEquals(found, service.getByFirstName("Ana"));
        assertNull(service.getByFirstName("Nadie"));
    }

    @Test
    void getBySecondName_returnsListWhenFound_nullWhenEmpty() {
        List<PersonPartner> found = List.of(partner("1"));
        when(repository.findBySecondName("María")).thenReturn(found);
        when(repository.findBySecondName("Nadie")).thenReturn(List.of());

        assertEquals(found, service.getBySecondName("María"));
        assertNull(service.getBySecondName("Nadie"));
    }

    @Test
    void getByShareNumber_returnsListWhenFound_nullWhenEmpty() {
        List<PersonPartner> found = List.of(partner("1"));
        when(repository.findByShareNumber(7L)).thenReturn(found);
        when(repository.findByShareNumber(99L)).thenReturn(List.of());

        assertEquals(found, service.getByShareNumber(7L));
        assertNull(service.getByShareNumber(99L));
    }

    @Test
    void getByComsuption_returnsConsumptionsWhenPartnerPresent_nullWhenAbsent() {
        PersonPartner p = partner("1");
        PartnerConsumption c = new PartnerConsumption();
        p.setConsumptions(List.of(c));
        when(repository.findByPersonId(1L)).thenReturn(Optional.of(p));
        when(repository.findByPersonId(5L)).thenReturn(Optional.empty());

        assertEquals(List.of(c), service.getByComsuption(1L));
        assertNull(service.getByComsuption(5L));
    }

    @Test
    void getAllPaged_delegatesToRepository() {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 10);
        org.springframework.data.domain.Page<PersonPartner> page =
                new org.springframework.data.domain.PageImpl<>(java.util.List.of(partner("1")));
        when(repository.findAll(pageable)).thenReturn(page);

        assertSame(page, service.getAllPaged(pageable));
    }
}
