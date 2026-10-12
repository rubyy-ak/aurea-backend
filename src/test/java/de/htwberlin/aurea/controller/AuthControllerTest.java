package de.htwberlin.aurea.controller;

import de.htwberlin.aurea.entity.AppUser;
import de.htwberlin.aurea.repository.AppUserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

	@Autowired
	private MockMvc mvc;

	@Autowired
	private AppUserRepository users;

	@Test
	void registerStoresOnlyPasswordHash() throws Exception {
		mvc.perform(post("/api/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"username\":\"chloe\",\"password\":\"geheim123\"}"))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.username").value("chloe"))
				.andExpect(jsonPath("$.token").isNotEmpty());

		AppUser user = users.findByUsernameIgnoreCase("chloe").orElseThrow();
		assertThat(user.getPasswordHash()).isNotEqualTo("geheim123").startsWith("$2");
	}

	@Test
	void emailAddressCanBeUsedAsUsername() throws Exception {
		mvc.perform(post("/api/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"username\":\"camille.dupont@example.com\",\"password\":\"geheim123\"}"))
				.andExpect(status().isCreated());
		mvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"username\":\"Camille.Dupont@example.com\",\"password\":\"geheim123\"}"))
				.andExpect(status().isOk());
	}

	@Test
	void duplicateUsernameIsRejected() throws Exception {
		String body = "{\"username\":\"doppelt\",\"password\":\"geheim123\"}";
		mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
				.andExpect(status().isCreated());
		mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
						.content("{\"username\":\"DOPPELT\",\"password\":\"geheim123\"}"))
				.andExpect(status().isConflict());
	}

	@Test
	void registerValidatesInput() throws Exception {
		mvc.perform(post("/api/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"username\":\"a\",\"password\":\"kurz\"}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.fieldErrors.username").exists())
				.andExpect(jsonPath("$.fieldErrors.password").exists());
	}

	@Test
	void loginWithWrongPasswordFails() throws Exception {
		mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
						.content("{\"username\":\"lea\",\"password\":\"richtig123\"}"))
				.andExpect(status().isCreated());

		mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
						.content("{\"username\":\"lea\",\"password\":\"falsch123\"}"))
				.andExpect(status().isUnauthorized());

		mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
						.content("{\"username\":\"lea\",\"password\":\"richtig123\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.token").isNotEmpty());
	}

	@Test
	void logoutInvalidatesToken() throws Exception {
		String auth = ApiTestSupport.registerUser(mvc);
		mvc.perform(get("/api/auth/me").header("Authorization", auth)).andExpect(status().isOk());

		mvc.perform(post("/api/auth/logout").header("Authorization", auth)).andExpect(status().isNoContent());

		mvc.perform(get("/api/auth/me").header("Authorization", auth)).andExpect(status().isUnauthorized());
	}

	@Test
	void protectedEndpointsRequireToken() throws Exception {
		mvc.perform(get("/api/perfumes")).andExpect(status().isUnauthorized());
		mvc.perform(get("/api/perfumes").header("Authorization", "Bearer erfunden"))
				.andExpect(status().isUnauthorized());
		mvc.perform(get("/api/health")).andExpect(status().isOk());
	}
}
