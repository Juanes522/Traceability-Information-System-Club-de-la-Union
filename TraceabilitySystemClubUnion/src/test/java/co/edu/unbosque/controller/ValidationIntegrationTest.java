package co.edu.unbosque.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
/**
 * Verifica de extremo a extremo que los fallos de validación se traducen a respuestas con detalle de campo.
 *
 * <p>Confirma que el manejador de excepciones está efectivamente conectado y que devuelve el campo y su mensaje, no un error
 * genérico. Comprueba también que unas credenciales con formato correcto pero incorrectas <strong>superan la validación y
 * llegan a la autenticación</strong>, es decir que la validación no se confunde con la autorización.
 */
class ValidationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void login_withBlankFields_returns400WithFieldErrors() throws Exception {
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.identification").exists())
                .andExpect(jsonPath("$.password").exists())
                .andExpect(content().string(not(containsString("Exception"))));
    }

    @Test
    void login_withWrongCredentials_reachesAuthNotValidation() throws Exception {
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"identification\":\"99999999\",\"password\":\"algosecreto\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void forgotPassword_withInvalidEmail_returns400() throws Exception {
        mockMvc.perform(post("/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"no-es-un-email\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.email").exists());
    }
}
