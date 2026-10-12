package de.htwberlin.aurea.controller;

import com.jayway.jsonpath.JsonPath;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

final class ApiTestSupport {

	private ApiTestSupport() {
	}

	/** Registriert einen frischen Nutzer und liefert den "Authorization"-Header-Wert. */
	static String registerUser(MockMvc mvc) throws Exception {
		String username = "user_" + UUID.randomUUID().toString().substring(0, 8);
		String body = mvc.perform(post("/api/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"username\":\"" + username + "\",\"password\":\"geheim123\"}"))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();
		return "Bearer " + JsonPath.read(body, "$.token");
	}

	static String perfumeJson(String name, String family, String status) {
		return """
				{"name":"%s","brand":"Maison Test","concentration":"EAU_DE_PARFUM","family":"%s",
				 "topNotes":"Bergamotte","heartNotes":"Rose","baseNotes":"Vanille",
				 "sizeMl":50,"priceEur":120.50,"rating":4,"status":"%s"}
				""".formatted(name, family, status);
	}

	static long createPerfume(MockMvc mvc, String auth, String name, String family, String status) throws Exception {
		String body = mvc.perform(post("/api/perfumes")
						.header("Authorization", auth)
						.contentType(MediaType.APPLICATION_JSON)
						.content(perfumeJson(name, family, status)))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();
		return ((Number) JsonPath.read(body, "$.id")).longValue();
	}
}
