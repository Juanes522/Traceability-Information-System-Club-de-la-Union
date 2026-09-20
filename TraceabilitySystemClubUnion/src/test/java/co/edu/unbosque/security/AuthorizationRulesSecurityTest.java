package co.edu.unbosque.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

/**
 * Pruebas de seguridad — Control de acceso por rol.
 *
 * Verifican que las reglas de autorizacion (SecurityConfig + @PreAuthorize) se
 * cumplen de extremo a extremo a traves de la cadena real de filtros de Spring
 * Security:
 *   - sin autenticacion  -> 401/403
 *   - rol insuficiente   -> 403
 *   - rol adecuado       -> no se deniega el acceso (no 401/403)
 *
 * Roles del sistema: ROLE_PARTNER, ROLE_MANAGER, ROLE_ADMIN.
 * Endpoints usados (sin parametros obligatorios, para que la denegacion sea limpia):
 *   - GET /audit                    -> hasRole('ADMIN')
 *   - GET /personpartner/getall     -> hasAnyRole('MANAGER','ADMIN')
 *   - GET /personpartner/me         -> solo autenticado (cualquier rol)
 */
@SpringBootTest
@AutoConfigureMockMvc
class AuthorizationRulesSecurityTest {

	private static final String ADMIN_ONLY = "/audit";
	private static final String MANAGER_OR_ADMIN = "/personpartner/getall";
	private static final String AUTHENTICATED_ONLY = "/personpartner/me";
	private static final String BY_ENVIRONMENT = "/partnerconsumption/by-environment/Bar";

	@Autowired
	private MockMvc mockMvc;

	/** Acepta 401 (no autenticado) o 403 (prohibido) como denegacion valida de acceso. */
	private static ResultMatcher accessDenied() {
		return result -> {
			int status = result.getResponse().getStatus();
			if (status != 401 && status != 403) {
				throw new AssertionError("Se esperaba 401 o 403 (acceso denegado) pero fue " + status);
			}
		};
	}

	/** El acceso NO fue denegado por autenticacion/autorizacion (cualquier estado distinto de 401/403). */
	private static ResultMatcher accessGranted() {
		return result -> {
			int status = result.getResponse().getStatus();
			if (status == 401 || status == 403) {
				throw new AssertionError("No se esperaba denegacion de acceso, pero fue " + status);
			}
		};
	}

	// ---------- Sin autenticacion ----------

	@Test
	void anonymous_isDeniedOnAdminEndpoint() throws Exception {
		mockMvc.perform(get(ADMIN_ONLY)).andExpect(accessDenied());
	}

	@Test
	void anonymous_isDeniedOnManagerEndpoint() throws Exception {
		mockMvc.perform(get(MANAGER_OR_ADMIN)).andExpect(accessDenied());
	}

	@Test
	void anonymous_isDeniedOnAuthenticatedEndpoint() throws Exception {
		mockMvc.perform(get(AUTHENTICATED_ONLY)).andExpect(accessDenied());
	}

	// ---------- Rol PARTNER (el mas bajo) ----------

	@Test
	void partner_isForbiddenOnAdminEndpoint() throws Exception {
		mockMvc.perform(get(ADMIN_ONLY).with(user("partner").roles("PARTNER")))
				.andExpect(accessDenied());
	}

	@Test
	void partner_isForbiddenOnManagerEndpoint() throws Exception {
		mockMvc.perform(get(MANAGER_OR_ADMIN).with(user("partner").roles("PARTNER")))
				.andExpect(accessDenied());
	}

	@Test
	void partner_isAllowedOnAuthenticatedEndpoint() throws Exception {
		mockMvc.perform(get(AUTHENTICATED_ONLY).with(user("partner").roles("PARTNER")))
				.andExpect(accessGranted());
	}

	@Test
	void partner_isForbiddenOnByEnvironmentEndpoint() throws Exception {
		mockMvc.perform(get(BY_ENVIRONMENT).with(user("partner").roles("PARTNER")))
				.andExpect(accessDenied());
	}

	// ---------- Rol MANAGER ----------

	@Test
	void manager_isAllowedOnManagerEndpoint() throws Exception {
		mockMvc.perform(get(MANAGER_OR_ADMIN).with(user("manager").roles("MANAGER")))
				.andExpect(accessGranted());
	}

	@Test
	void manager_isForbiddenOnAdminEndpoint() throws Exception {
		mockMvc.perform(get(ADMIN_ONLY).with(user("manager").roles("MANAGER")))
				.andExpect(accessDenied());
	}

	@Test
	void manager_isAllowedOnByEnvironmentEndpoint() throws Exception {
		mockMvc.perform(get(BY_ENVIRONMENT).with(user("manager").roles("MANAGER")))
				.andExpect(accessGranted());
	}

	// ---------- Rol ADMIN ----------

	@Test
	void admin_isAllowedOnAdminEndpoint() throws Exception {
		mockMvc.perform(get(ADMIN_ONLY).with(user("admin").roles("ADMIN")))
				.andExpect(accessGranted());
	}

	@Test
	void admin_isAllowedOnManagerEndpoint() throws Exception {
		mockMvc.perform(get(MANAGER_OR_ADMIN).with(user("admin").roles("ADMIN")))
				.andExpect(accessGranted());
	}
}
