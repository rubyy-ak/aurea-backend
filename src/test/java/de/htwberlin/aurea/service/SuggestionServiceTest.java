package de.htwberlin.aurea.service;

import de.htwberlin.aurea.dto.SuggestionResponse;
import de.htwberlin.aurea.entity.AppUser;
import de.htwberlin.aurea.entity.Concentration;
import de.htwberlin.aurea.entity.Perfume;
import de.htwberlin.aurea.entity.PerfumeStatus;
import de.htwberlin.aurea.entity.ScentFamily;
import de.htwberlin.aurea.repository.PerfumeRepository;
import de.htwberlin.aurea.weather.CurrentWeather;
import de.htwberlin.aurea.weather.WeatherClient;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SuggestionServiceTest {

	private final PerfumeRepository repo = mock(PerfumeRepository.class);
	private final WeatherClient weather = mock(WeatherClient.class);
	private final Clock julyClock = Clock.fixed(Instant.parse("2026-07-15T10:00:00Z"), ZoneOffset.UTC);
	private final SuggestionService service = new SuggestionService(repo, weather, julyClock);

	private static Perfume perfume(String name, ScentFamily family, LocalDate lastWorn) {
		Perfume p = new Perfume(new AppUser("x", "hash"));
		p.setName(name);
		p.setBrand("Brand");
		p.setConcentration(Concentration.EAU_DE_PARFUM);
		p.setFamily(family);
		p.setStatus(PerfumeStatus.OWNED);
		if (lastWorn != null) {
			p.wear(lastWorn);
		}
		return p;
	}

	@Test
	void prefersLeastRecentlyWornMatchingFamily() {
		when(weather.currentWeather()).thenReturn(Optional.of(new CurrentWeather(25, 0)));
		when(repo.findByOwnerIdAndStatus(1L, PerfumeStatus.OWNED)).thenReturn(List.of(
				perfume("Gestern", ScentFamily.CITRUS, LocalDate.of(2026, 7, 14)),
				perfume("Lange her", ScentFamily.FRESH, LocalDate.of(2026, 5, 1)),
				perfume("Schwer", ScentFamily.ORIENTAL, null)));

		SuggestionResponse result = service.suggest(1L);

		assertThat(result.perfume().name()).isEqualTo("Lange her");
		assertThat(result.temperatureC()).isEqualTo(25.0);
	}

	@Test
	void fallsBackToSeasonWhenWeatherUnavailable() {
		when(weather.currentWeather()).thenReturn(Optional.empty());
		when(repo.findByOwnerIdAndStatus(1L, PerfumeStatus.OWNED)).thenReturn(List.of(
				perfume("Holz", ScentFamily.WOODY, null),
				perfume("Zitrone", ScentFamily.CITRUS, null)));

		SuggestionResponse result = service.suggest(1L);

		// Juli -> warme Jahreszeit -> leichter Duft
		assertThat(result.perfume().name()).isEqualTo("Zitrone");
		assertThat(result.temperatureC()).isNull();
	}

	@Test
	void emptyCollectionGivesHint() {
		when(weather.currentWeather()).thenReturn(Optional.empty());
		when(repo.findByOwnerIdAndStatus(1L, PerfumeStatus.OWNED)).thenReturn(List.of());

		SuggestionResponse result = service.suggest(1L);

		assertThat(result.perfume()).isNull();
		assertThat(result.reason()).contains("Sammlung");
	}
}
