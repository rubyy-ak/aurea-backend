package de.htwberlin.aurea.controller;

import de.htwberlin.aurea.weather.CurrentWeather;
import de.htwberlin.aurea.weather.WeatherClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static de.htwberlin.aurea.controller.ApiTestSupport.createPerfume;
import static de.htwberlin.aurea.controller.ApiTestSupport.perfumeJson;
import static de.htwberlin.aurea.controller.ApiTestSupport.registerUser;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PerfumeControllerTest {

	@Autowired
	private MockMvc mvc;

	@MockitoBean
	private WeatherClient weatherClient;

	private String auth;

	@BeforeEach
	void setUp() throws Exception {
		auth = registerUser(mvc);
	}

	@Test
	void createAndReadPerfume() throws Exception {
		long id = createPerfume(mvc, auth, "Rose Noire", "FLORAL", "OWNED");

		mvc.perform(get("/api/perfumes/" + id).header("Authorization", auth))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name").value("Rose Noire"))
				.andExpect(jsonPath("$.family").value("FLORAL"))
				.andExpect(jsonPath("$.priceEur").value(120.5))
				.andExpect(jsonPath("$.status").value("OWNED"));
	}

	@Test
	void createRejectsInvalidPerfume() throws Exception {
		mvc.perform(post("/api/perfumes").header("Authorization", auth)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"name\":\"\",\"brand\":\"X\",\"rating\":9,\"sizeMl\":0}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.fieldErrors.name").exists())
				.andExpect(jsonPath("$.fieldErrors.rating").exists())
				.andExpect(jsonPath("$.fieldErrors.sizeMl").exists())
				.andExpect(jsonPath("$.fieldErrors.family").exists());
	}

	@Test
	void listFiltersByStatusFamilyAndQuery() throws Exception {
		createPerfume(mvc, auth, "Neroli Bleu", "CITRUS", "OWNED");
		createPerfume(mvc, auth, "Oud Royal", "WOODY", "OWNED");
		createPerfume(mvc, auth, "Ambre Doré", "ORIENTAL", "WISHLIST");

		mvc.perform(get("/api/perfumes").param("status", "OWNED").header("Authorization", auth))
				.andExpect(jsonPath("$", hasSize(2)));
		mvc.perform(get("/api/perfumes").param("family", "WOODY").header("Authorization", auth))
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].name").value("Oud Royal"));
		mvc.perform(get("/api/perfumes").param("q", "ambre").header("Authorization", auth))
				.andExpect(jsonPath("$", hasSize(1)));
		mvc.perform(get("/api/perfumes").param("sort", "name").header("Authorization", auth))
				.andExpect(jsonPath("$", hasSize(3)));
	}

	@Test
	void updateAndDeletePerfume() throws Exception {
		long id = createPerfume(mvc, auth, "Alt", "FRESH", "OWNED");

		mvc.perform(put("/api/perfumes/" + id).header("Authorization", auth)
						.contentType(MediaType.APPLICATION_JSON)
						.content(perfumeJson("Neu", "AQUATIC", "OWNED")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name").value("Neu"))
				.andExpect(jsonPath("$.family").value("AQUATIC"));

		mvc.perform(delete("/api/perfumes/" + id).header("Authorization", auth))
				.andExpect(status().isNoContent());
		mvc.perform(get("/api/perfumes/" + id).header("Authorization", auth))
				.andExpect(status().isNotFound());
	}

	@Test
	void usersCannotSeeEachOthersPerfumes() throws Exception {
		long id = createPerfume(mvc, auth, "Privat", "LEATHER", "OWNED");
		String otherAuth = registerUser(mvc);

		mvc.perform(get("/api/perfumes").header("Authorization", otherAuth))
				.andExpect(jsonPath("$", hasSize(0)));
		mvc.perform(get("/api/perfumes/" + id).header("Authorization", otherAuth))
				.andExpect(status().isNotFound());
		mvc.perform(delete("/api/perfumes/" + id).header("Authorization", otherAuth))
				.andExpect(status().isNotFound());
	}

	@Test
	void purchaseMovesWishlistPerfumeToCollection() throws Exception {
		long wish = createPerfume(mvc, auth, "Traum", "GOURMAND", "WISHLIST");

		mvc.perform(post("/api/perfumes/" + wish + "/purchase").header("Authorization", auth))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("OWNED"));
		mvc.perform(get("/api/perfumes").param("status", "WISHLIST").header("Authorization", auth))
				.andExpect(jsonPath("$", hasSize(0)));
		// Schon in der Sammlung -> nicht noch einmal kaufbar.
		mvc.perform(post("/api/perfumes/" + wish + "/purchase").header("Authorization", auth))
				.andExpect(status().isConflict());
	}

	@Test
	void suggestionUsesWeather() throws Exception {
		createPerfume(mvc, auth, "Sommerbrise", "CITRUS", "OWNED");
		createPerfume(mvc, auth, "Winterfeuer", "ORIENTAL", "OWNED");

		when(weatherClient.currentWeather()).thenReturn(Optional.of(new CurrentWeather(4.0, 3)));
		mvc.perform(get("/api/perfumes/suggestion").header("Authorization", auth))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.perfume.name").value("Winterfeuer"))
				.andExpect(jsonPath("$.temperatureC").value(4.0))
				.andExpect(jsonPath("$.weather").value("bewölkt"));

		when(weatherClient.currentWeather()).thenReturn(Optional.of(new CurrentWeather(27.0, 0)));
		mvc.perform(get("/api/perfumes/suggestion").header("Authorization", auth))
				.andExpect(jsonPath("$.perfume.name").value("Sommerbrise"));
	}
}
