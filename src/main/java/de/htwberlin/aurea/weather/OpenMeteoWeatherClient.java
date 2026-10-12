package de.htwberlin.aurea.weather;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Duration;
import java.util.Optional;

/** Externe API: Open-Meteo (kostenlos, ohne API-Key). */
@Component
public class OpenMeteoWeatherClient implements WeatherClient {

	private static final Logger log = LoggerFactory.getLogger(OpenMeteoWeatherClient.class);

	private final RestClient restClient;
	private final double latitude;
	private final double longitude;

	public OpenMeteoWeatherClient(
			@Value("${app.weather.base-url:https://api.open-meteo.com}") String baseUrl,
			@Value("${app.weather.latitude:52.52}") double latitude,
			@Value("${app.weather.longitude:13.405}") double longitude) {
		var requestFactory = new SimpleClientHttpRequestFactory();
		requestFactory.setConnectTimeout(Duration.ofSeconds(3));
		requestFactory.setReadTimeout(Duration.ofSeconds(3));
		this.restClient = RestClient.builder().baseUrl(baseUrl).requestFactory(requestFactory).build();
		this.latitude = latitude;
		this.longitude = longitude;
	}

	record Response(Current current) {
	}

	record Current(double temperature_2m, int weather_code) {
	}

	@Override
	public Optional<CurrentWeather> currentWeather() {
		try {
			Response response = restClient.get()
					.uri(uri -> uri.path("/v1/forecast")
							.queryParam("latitude", latitude)
							.queryParam("longitude", longitude)
							.queryParam("current", "temperature_2m,weather_code")
							.build())
					.retrieve()
					.body(Response.class);
			if (response == null || response.current() == null) {
				return Optional.empty();
			}
			return Optional.of(new CurrentWeather(response.current().temperature_2m(),
					response.current().weather_code()));
		} catch (RestClientException e) {
			log.warn("Open-Meteo nicht erreichbar: {}", e.getMessage());
			return Optional.empty();
		}
	}
}
