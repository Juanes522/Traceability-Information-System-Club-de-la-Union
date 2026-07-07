package co.unbosque.service;

import java.time.LocalDateTime;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import co.unbosque.model.RevokedToken;
import co.unbosque.repository.RevokedTokenRepository;

@Service
public class TokenBlacklistService {

    private final RevokedTokenRepository repository;

    public TokenBlacklistService(RevokedTokenRepository repository) {
        this.repository = repository;
    }

    public void revoke(String jti, LocalDateTime expiryDate) {
        if (jti == null) {
            return;
        }
        repository.save(new RevokedToken(jti, expiryDate));
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
