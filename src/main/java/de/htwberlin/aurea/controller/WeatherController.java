package de.htwberlin.aurea.controller;

import de.htwberlin.aurea.weather.WeatherClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/** Öffentlich: zeigt, ob das Backend Open-Meteo erreicht und welches Wetter es gerade sieht. */
@RestController
public class WeatherController {

	private final WeatherClient weatherClient;

	public WeatherController(WeatherClient weatherClient) {
		this.weatherClient = weatherClient;
	}

	@GetMapping("/api/weather")
	public Map<String, Object> weather() {
		Map<String, Object> result = new LinkedHashMap<>();
		weatherClient.currentWeather().ifPresentOrElse(w -> {
			result.put("available", true);
			result.put("temperatureC", w.temperatureC());
			result.put("weather", w.description());
		}, () -> result.put("available", false));
		return result;
	}
}
