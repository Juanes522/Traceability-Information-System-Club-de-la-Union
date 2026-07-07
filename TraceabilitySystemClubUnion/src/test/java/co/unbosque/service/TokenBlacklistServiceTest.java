package co.unbosque.service;

import co.unbosque.model.RevokedToken;
import co.unbosque.repository.RevokedTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TokenBlacklistServiceTest {

    private RevokedTokenRepository repository;
    private TokenBlacklistService service;

    @BeforeEach
    void setUp() {
        repository = mock(RevokedTokenRepository.class);
        service = new TokenBlacklistService(repository);
    }

    @Test
    void revoke_savesJtiWithExpiry() {
        LocalDateTime expiry = LocalDateTime.now().plusHours(8);
        service.revoke("jti-123", expiry);

        ArgumentCaptor<RevokedToken> captor = ArgumentCaptor.forClass(RevokedToken.class);
        verify(repository).save(captor.capture());
        assertEquals("jti-123", captor.getValue().getJti());
        assertEquals(expiry, captor.getValue().getExpiryDate());
    }

    @Test
    void revoke_isIdempotent_doesNotSaveDuplicateJti() {
        when(repository.existsByJti("jti-dup")).thenReturn(true);
        service.revoke("jti-dup", LocalDateTime.now().plusHours(1));
        verify(repository, never()).save(any());
    }

    @Test
    void isRevoked_reflectsRepository() {
        when(repository.existsByJti("revocado")).thenReturn(true);
        when(repository.existsByJti("vigente")).thenReturn(false);
        assertTrue(service.isRevoked("revocado"));
        assertFalse(service.isRevoked("vigente"));
    }

    @Test
    void isRevoked_falseForNullJti() {
        assertFalse(service.isRevoked(null));
    }
}
