package co.edu.unbosque.service;

import java.time.LocalDateTime;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import co.edu.unbosque.model.PersonPartner;
import co.edu.unbosque.model.RevokedToken;
import co.edu.unbosque.repository.RevokedTokenRepository;

@Service
public class TokenBlacklistService {

    private final RevokedTokenRepository repository;

    public TokenBlacklistService(RevokedTokenRepository repository) {
        this.repository = repository;
    }

    public void revoke(String jti, LocalDateTime expiryDate) {
        revoke(jti, expiryDate, null);
    }

    public void revoke(String jti, LocalDateTime expiryDate, PersonPartner person) {
        if (jti == null || repository.existsByJti(jti)) {
            return;
        }
        repository.save(new RevokedToken(jti, expiryDate, person));
    }

    public boolean isRevoked(String jti) {
        if (jti == null) {
            return false;
        }
        return repository.existsByJti(jti);
    }

    @Scheduled(fixedRate = 3600000)
    public void purgeExpired() {
        repository.deleteByExpiryDateBefore(LocalDateTime.now());
    }
}
