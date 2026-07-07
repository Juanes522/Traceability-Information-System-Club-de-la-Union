package co.unbosque.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class JwtUtilTest {

    private static final String SECRET =
            "test-secret-key-that-is-long-enough-for-hs256-algorithm-0123456789";

    private JwtUtil jwtUtil;

    private UserDetails user(String username) {
        return new User(username, "x",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_PARTNER")));
    }

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil(SECRET);
    }

    @Test
    void generatedToken_carriesNonNullJti() {
        String token = jwtUtil.generateToken(user("1234567890"));
        assertNotNull(jwtUtil.extractJti(token));
    }

    @Test
    void twoTokens_haveDifferentJti() {
        String first = jwtUtil.generateToken(user("1234567890"));
        String second = jwtUtil.generateToken(user("1234567890"));
        assertNotEquals(jwtUtil.extractJti(first), jwtUtil.extractJti(second));
    }

    @Test
    void extractUsername_returnsSubject() {
        String token = jwtUtil.generateToken(user("1234567890"));
        assertEquals("1234567890", jwtUtil.extractUsername(token));
    }
}
