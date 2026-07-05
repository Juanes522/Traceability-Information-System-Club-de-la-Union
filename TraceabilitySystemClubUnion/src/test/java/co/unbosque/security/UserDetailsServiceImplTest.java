package co.unbosque.security;

import co.unbosque.model.PersonPartner;
import co.unbosque.repository.PersonPartnerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserDetailsServiceImplTest {

    private PersonPartnerRepository repository;
    private UserDetailsServiceImpl service;

    @BeforeEach
    void setUp() {
        repository = mock(PersonPartnerRepository.class);
        service = new UserDetailsServiceImpl();
        ReflectionTestUtils.setField(service, "personPartnerRepository", repository);
    }

    private UserDetails userDetails(String username) {
        return new User(username, "plaintext",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_PARTNER")));
    }

    @Test
    void updatePassword_persistsNewHashAndReturnsUpdatedUser() {
        PersonPartner titular = new PersonPartner();
        titular.setIdentification("1234567890");
        titular.setPassword("plaintext");
        when(repository.findByIdentification("1234567890")).thenReturn(Optional.of(titular));

        UserDetails updated = service.updatePassword(userDetails("1234567890"), "{bcrypt}$2a$10$nuevoHash");

        ArgumentCaptor<PersonPartner> captor = ArgumentCaptor.forClass(PersonPartner.class);
        verify(repository).save(captor.capture());
        assertEquals("{bcrypt}$2a$10$nuevoHash", captor.getValue().getPassword());
        assertEquals("{bcrypt}$2a$10$nuevoHash", updated.getPassword());
        assertEquals("1234567890", updated.getUsername());
    }

    @Test
    void updatePassword_throwsWhenUserNotFound() {
        when(repository.findByIdentification("desconocido")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> service.updatePassword(userDetails("desconocido"), "{bcrypt}$2a$10$x"));
    }
}
