package co.edu.unbosque.service;

import co.edu.unbosque.model.PersonPartner;
import co.edu.unbosque.repository.PersonPartnerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PersonPartnerServiceGetByEmailTest {

    private PersonPartnerRepository repository;
    private PersonPartnerService service;
    private PersonPartner conEmail;
    private PersonPartner sinEmail;

    @BeforeEach
    void setUp() {
        repository = mock(PersonPartnerRepository.class);
        service = new PersonPartnerService();
        ReflectionTestUtils.setField(service, "partnerRepo", repository);

        sinEmail = new PersonPartner();
        sinEmail.setIdentification("111");

        conEmail = new PersonPartner();
        conEmail.setIdentification("222");
        conEmail.setEmail(new String[] { "ana@example.com", "familia@example.com" });

        when(repository.findAll()).thenReturn(List.of(sinEmail, conEmail));
    }

    @Test
    void getByEmail_findsPartnerByAnyOfItsEmails() {
        assertSame(conEmail, service.getByEmail("ana@example.com"));
        assertSame(conEmail, service.getByEmail("familia@example.com"));
    }

    @Test
    void getByEmail_isCaseInsensitiveAndTrimsInput() {
        assertSame(conEmail, service.getByEmail("  ANA@EXAMPLE.COM  "));
    }

    @Test
    void getByEmail_returnsNullWhenNoMatch() {
        assertNull(service.getByEmail("nadie@example.com"));
        assertNull(service.getByEmail(null));
        assertNull(service.getByEmail("   "));
    }
}
