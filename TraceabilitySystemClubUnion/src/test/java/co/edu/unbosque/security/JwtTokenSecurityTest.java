package co.edu.unbosque.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

/**
 * Pruebas de seguridad — Validacion de tokens JWT.
 *
 * Verifican que un endpoint protegido NUNCA concede acceso con un token invalido:
 *   - sin token
 *   - token malformado
 *   - token con firma invalida (firmado con otra clave)
 *   - token expirado (firmado con la clave correcta, pero vencido)
 *
 * En todos los casos el JwtAuthenticationFilter no debe autenticar la peticion,
 * por lo que el endpoint responde 401/403. Los tokens se construyen para que el
 * parseo falle (firma/expiracion/formato), de modo que nunca se consulte un usuario
 * inexistente en la base de datos.
 */
@SpringBootTest
@AutoConfigureMockMvc
class JwtTokenSecurityTest {

	private static final String PROTECTED_ENDPOINT = "/personpartner/getall";

	@Autowired
	private MockMvc mockMvc;

	@Value("${jwt.secret}")
	private String jwtSecret;

	private static ResultMatcher accessDenied() {
		return result -> {
			int status = result.getResponse().getStatus();
			if (status != 401 && status != 403) {
				throw new AssertionError("Se esperaba 401 o 403 (token rechazado) pero fue " + status);
			}
		};
	}

	@Test
	void noToken_isDenied() throws Exception {
		mockMvc.perform(get(PROTECTED_ENDPOINT)).andExpect(accessDenied());
	}

	@Test
	void malformedToken_isDenied() throws Exception {
		mockMvc.perform(get(PROTECTED_ENDPOINT)
				.header("Authorization", "Bearer esto-no-es-un-jwt"))
				.andExpect(accessDenied());
	}

	@Test
	void tamperedSignatureToken_isDenied() throws Exception {
		// Firmado con una clave distinta: la firma no valida contra la clave real del servidor.
		SecretKey otherKey = Keys.hmacShaKeyFor(
				"clave-distinta-suficientemente-larga-para-hs256-9876543210abcdef".getBytes(StandardCharsets.UTF_8));
		long now = System.currentTimeMillis();
		String token = Jwts.builder()
				.setSubject("99999999")
				.setId(UUID.randomUUID().toString())
				.setIssuedAt(new Date(now))
				.setExpiration(new Date(now + 3600_000))
				.signWith(otherKey, SignatureAlgorithm.HS256)
				.compact();

		mockMvc.perform(get(PROTECTED_ENDPOINT)
				.header("Authorization", "Bearer " + token))
				.andExpect(accessDenied());
	}

	@Test
	void expiredToken_isDenied() throws Exception {
		// Firmado con la clave correcta pero ya vencido.
		SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
		long now = System.currentTimeMillis();
		String token = Jwts.builder()
				.setSubject("99999999")
				.setId(UUID.randomUUID().toString())
				.setIssuedAt(new Date(now - 7_200_000))
				.setExpiration(new Date(now - 3_600_000))
				.signWith(key, SignatureAlgorithm.HS256)
				.compact();

		mockMvc.perform(get(PROTECTED_ENDPOINT)
				.header("Authorization", "Bearer " + token))
				.andExpect(accessDenied());
	}
}
