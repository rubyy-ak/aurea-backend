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
		LocalDate today = LocalDate.now(clock);
		Optional<CurrentWeather> weather = weatherClient.currentWeather();
		boolean warm = weather.map(w -> w.temperatureC() >= WARM_THRESHOLD_C)
				.orElseGet(() -> isWarmSeason(today));

		Double temperature = weather.map(CurrentWeather::temperatureC).orElse(null);
		String description = weather.map(CurrentWeather::description).orElse(null);

		List<Perfume> owned = perfumes.findByOwnerIdAndStatus(userId, PerfumeStatus.OWNED);
		if (owned.isEmpty()) {
			return new SuggestionResponse(null, temperature, description,
					"Füge zuerst ein Parfüm zu deiner Sammlung hinzu.");
		}

		// Nur Düfte, die zum Wetter passen – gibt es keine, kommen alle in Frage.
		List<Perfume> matching = owned.stream().filter(p -> p.getFamily().isLight() == warm).toList();
		boolean fits = !matching.isEmpty();
		List<Perfume> candidates = (fits ? matching : owned).stream()
				.sorted(Comparator.comparing(Perfume::getCreatedAt).thenComparing(Perfume::getName))
				.toList();

		// Jeden Tag ein anderer Kandidat, am selben Tag aber immer derselbe.
		Perfume pick = candidates.get(today.getDayOfYear() % candidates.size());

		String mood = warm ? "warm – Zeit für etwas Leichtes und Frisches" : "kühl – perfekt für einen warmen, tiefen Duft";
		String reason = fits
				? "Es ist " + mood + "."
				: "Es ist " + mood + ". In deiner Sammlung passt gerade nichts perfekt – wie wäre es hiermit?";
		return new SuggestionResponse(PerfumeResponse.from(pick), temperature, description, reason);
	}

	private static boolean isWarmSeason(LocalDate day) {
		Month month = day.getMonth();
		return month.getValue() >= Month.MAY.getValue() && month.getValue() <= Month.SEPTEMBER.getValue();
	}
}
