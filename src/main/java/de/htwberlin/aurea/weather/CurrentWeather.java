package de.htwberlin.aurea.weather;

/** Aktuelles Wetter: Temperatur in °C und WMO-Wettercode. */
public record CurrentWeather(double temperatureC, int weatherCode) {

	/** Übernimmt Werte vom Browser nur, wenn sie vollständig und realistisch sind. */
	public static java.util.Optional<CurrentWeather> ofPlausible(Double temperatureC, Integer weatherCode) {
		if (temperatureC == null || weatherCode == null || temperatureC.isNaN()
				|| temperatureC < -60 || temperatureC > 60 || weatherCode < 0 || weatherCode > 99) {
			return java.util.Optional.empty();
		}
		return java.util.Optional.of(new CurrentWeather(temperatureC, weatherCode));
	}

	/** Kurzbeschreibung des WMO-Codes (https://open-meteo.com/en/docs). */
	public String description() {
		if (weatherCode == 0) return "klar";
		if (weatherCode <= 3) return "bewölkt";
		if (weatherCode <= 48) return "neblig";
		if (weatherCode <= 67) return "regnerisch";
		if (weatherCode <= 77) return "verschneit";
		if (weatherCode <= 82) return "Regenschauer";
		if (weatherCode <= 86) return "Schneeschauer";
		return "Gewitter";
	}
}
