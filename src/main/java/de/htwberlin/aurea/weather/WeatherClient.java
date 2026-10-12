package de.htwberlin.aurea.weather;

import java.util.Optional;

public interface WeatherClient {

	/** Liefert das aktuelle Wetter oder {@code Optional.empty()}, wenn der Dienst nicht erreichbar ist. */
	Optional<CurrentWeather> currentWeather();
}
