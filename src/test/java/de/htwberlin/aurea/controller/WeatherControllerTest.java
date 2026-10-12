package de.htwberlin.aurea.controller;

import de.htwberlin.aurea.weather.CurrentWeather;
import de.htwberlin.aurea.weather.WeatherClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class WeatherControllerTest {

	@Autowired
	private MockMvc mvc;

	@MockitoBean
	private WeatherClient weatherClient;

	@Test
	void showsWeatherWithoutLogin() throws Exception {
		when(weatherClient.currentWeather()).thenReturn(Optional.of(new CurrentWeather(14.2, 0)));
		mvc.perform(get("/api/weather"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.available").value(true))
				.andExpect(jsonPath("$.temperatureC").value(14.2))
				.andExpect(jsonPath("$.weather").value("klar"));
	}

	@Test
	void reportsUnavailableWeather() throws Exception {
		when(weatherClient.currentWeather()).thenReturn(Optional.empty());
		mvc.perform(get("/api/weather"))
				.andExpect(jsonPath("$.available").value(false));
	}
}
