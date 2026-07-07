package co.unbosque.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityHeadersTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void publicEndpoint_returnsHardenedSecurityHeaders() throws Exception {
        mockMvc.perform(get("/v3/api-docs").secure(true))
                .andExpect(header().string("Content-Security-Policy",
                        "default-src 'self'; frame-ancestors 'none'; object-src 'none'; base-uri 'self'"))
                .andExpect(header().string("X-Frame-Options", "DENY"))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(header().string("Strict-Transport-Security", containsString("max-age=31536000")))
                .andExpect(header().string("Strict-Transport-Security", containsString("includeSubDomains")));
    }
}
