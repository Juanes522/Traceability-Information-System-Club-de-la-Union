package co.unbosque.security;

import co.unbosque.service.TokenBlacklistService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JwtAuthenticationFilterTest {

    private JwtUtil jwtUtil;
    private UserDetailsServiceImpl userDetailsService;
    private TokenBlacklistService tokenBlacklist;
    private JwtAuthenticationFilter filter;

    private UserDetails user() {
        return new User("1234567890", "x",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_PARTNER")));
    }

    @BeforeEach
    void setUp() {
        jwtUtil = mock(JwtUtil.class);
        userDetailsService = mock(UserDetailsServiceImpl.class);
        tokenBlacklist = mock(TokenBlacklistService.class);
        filter = new JwtAuthenticationFilter();
        ReflectionTestUtils.setField(filter, "jwtUtil", jwtUtil);
        ReflectionTestUtils.setField(filter, "userDetailsService", userDetailsService);
        ReflectionTestUtils.setField(filter, "tokenBlacklist", tokenBlacklist);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private MockHttpServletResponse runWithToken() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/personpartner/me");
        request.addHeader("Authorization", "Bearer token-abc");
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(jwtUtil.extractUsername("token-abc")).thenReturn("1234567890");
        when(userDetailsService.loadUserByUsername("1234567890")).thenReturn(user());
        when(jwtUtil.validateToken("token-abc", user())).thenReturn(true);
        when(jwtUtil.extractJti("token-abc")).thenReturn("jti-1");
        filter.doFilter(request, response, new MockFilterChain());
        return response;
    }

    @Test
    void authenticates_whenTokenValidAndNotRevoked() throws Exception {
        when(tokenBlacklist.isRevoked("jti-1")).thenReturn(false);
        runWithToken();
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doesNotAuthenticate_whenTokenRevoked() throws Exception {
        when(tokenBlacklist.isRevoked("jti-1")).thenReturn(true);
        runWithToken();
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
}
