package co.edu.unbosque.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas de seguridad — Politica de contrasenas.
 *
 * Verifican que la restriccion @StrongPassword se aplica en el API (no solo en el
 * validador aislado) usando el endpoint publico POST /auth/reset-password:
 *   - contrasena debil  -> 400 con error de validacion sobre 'newPassword'
 *   - contrasena fuerte -> pasa la validacion y llega a la logica del token
 *     (400 por token invalido, pero SIN error de validacion sobre 'newPassword')
 */
@SpringBootTest
@AutoConfigureMockMvc
class PasswordPolicySecurityTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void resetPassword_withWeakPassword_isRejectedByPolicy() throws Exception {
		String body = "{\"token\":\"cualquier-token\",\"newPassword\":\"123\"}";

		mockMvc.perform(post("/auth/reset-password")
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.newPassword").exists());
	}

	@Test
	void resetPassword_withStrongPassword_passesPolicy() throws Exception {
		// Contrasena que cumple la politica; el 400 se debe al token invalido,
		// no a la validacion de la contrasena.
		String body = "{\"token\":\"token-invalido\",\"newPassword\":\"Str0ng!Passw0rd\"}";

		mockMvc.perform(post("/auth/reset-password")
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
				.andExpect(status().isBadRequest())
				.andExpect(content().string(not(containsString("newPassword"))));
	}
}
