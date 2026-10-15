package de.htwberlin.aurea.service;

import de.htwberlin.aurea.dto.PerfumeResponse;
import de.htwberlin.aurea.dto.SuggestionResponse;
import de.htwberlin.aurea.entity.Perfume;
import de.htwberlin.aurea.entity.PerfumeStatus;
import de.htwberlin.aurea.repository.PerfumeRepository;
import de.htwberlin.aurea.weather.CurrentWeather;
import de.htwberlin.aurea.weather.WeatherClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.Month;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/** Schlägt anhand des aktuellen Wetters einen Duft aus der Sammlung vor. */
@Service
public class SuggestionService {

	/** Ab dieser Temperatur gelten leichte, frische Düfte als passender. */
	static final double WARM_THRESHOLD_C = 18.0;

	private final PerfumeRepository perfumes;
	private final WeatherClient weatherClient;
	private final Clock clock;

	public SuggestionService(PerfumeRepository perfumes, WeatherClient weatherClient, Clock clock) {
		this.perfumes = perfumes;
		this.weatherClient = weatherClient;
		this.clock = clock;
	}

	@Transactional(readOnly = true)
	public SuggestionResponse suggest(Long userId) {
		return suggest(userId, Optional.empty());
	}

	/**
	 * @param browserWeather Wetter, das der Browser bereits bei Open-Meteo abgefragt hat. Fehlt es,
	 *                       fragt das Backend selbst nach.
	 */
	@Transactional(readOnly = true)
	public SuggestionResponse suggest(Long userId, Optional<CurrentWeather> browserWeather) {
		LocalDate today = LocalDate.now(clock);
		Optional<CurrentWeather> weather = browserWeather.isPresent() ? browserWeather : weatherClient.currentWeather();
		boolean warm = weather.map(w -> w.temperatureC() >= WARM_THRESHOLD_C)
				.orElseGet(() -> isWarmSeason(today));

		Double temperature = weather.map(CurrentWeather::temperatureC).orElse(null);
		String description = weather.map(CurrentWeather::description).orElse(null);

		List<Perfume> owned = perfumes.findByOwnerIdAndStatus(userId, PerfumeStatus.OWNED);
		if (owned.isEmpty()) {
			return new SuggestionResponse(
					null,
					temperature,
					description,
					"Füge zuerst ein Parfüm zu deiner Sammlung hinzu.",
					"EMPTY_COLLECTION"
			);
		}

		List<Perfume> matching = owned.stream()
				.filter(p -> p.getFamily().isLight() == warm)
				.toList();

		if (matching.isEmpty()) {
			return new SuggestionResponse(
					null,
					temperature,
					description,
					"Heute passt nichts perfekt aus deiner Sammlung.",
					"NO_MATCH"
			);
		}

		List<Perfume> favorites = matching.stream()
				.filter(Perfume::isFavorite)
				.toList();

		List<Perfume> candidates = (favorites.isEmpty() ? matching : favorites).stream()
				.sorted(Comparator.comparing(Perfume::getCreatedAt).thenComparing(Perfume::getName))
				.toList();

		Perfume pick = candidates.get(today.getDayOfYear() % candidates.size());

		String mood = warm
				? "warm – Zeit für etwas Leichtes und Frisches"
				: "kühl – perfekt für einen warmen, tiefen Duft";

		return new SuggestionResponse(
				PerfumeResponse.from(pick),
				temperature,
				description,
				"Es ist " + mood + ".",
				"MATCH"
		);
	}

	private static boolean isWarmSeason(LocalDate day) {
		Month month = day.getMonth();
		return month.getValue() >= Month.MAY.getValue() && month.getValue() <= Month.SEPTEMBER.getValue();
	}
}