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
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SuggestionServiceTest {

	private final PerfumeRepository repo = mock(PerfumeRepository.class);
	private final WeatherClient weather = mock(WeatherClient.class);

	private SuggestionService serviceOn(String isoInstant) {
		return new SuggestionService(repo, weather, Clock.fixed(Instant.parse(isoInstant), ZoneOffset.UTC));
	}

	private static Perfume perfume(String name, ScentFamily family) {
		Perfume p = new Perfume(new AppUser("x", "hash"));
		p.setName(name);
		p.setBrand("Brand");
		p.setConcentration(Concentration.EAU_DE_PARFUM);
		p.setFamily(family);
		p.setStatus(PerfumeStatus.OWNED);
		return p;
	}

	@Test
	void picksFamilyMatchingTheWeather() {
		when(weather.currentWeather()).thenReturn(Optional.of(new CurrentWeather(4, 3)));
		when(repo.findByOwnerIdAndStatus(1L, PerfumeStatus.OWNED)).thenReturn(List.of(
				perfume("Zitrone", ScentFamily.CITRUS),
				perfume("Holz", ScentFamily.WOODY)));

		SuggestionResponse result = serviceOn("2026-10-12T10:00:00Z").suggest(1L);

		assertThat(result.perfume().name()).isEqualTo("Holz");
		assertThat(result.temperatureC()).isEqualTo(4.0);
		assertThat(result.weather()).isEqualTo("bewölkt");
	}

	@Test
	void rotatesBetweenMatchingPerfumesDaily() {
		when(weather.currentWeather()).thenReturn(Optional.of(new CurrentWeather(25, 0)));
		List<Perfume> owned = List.of(perfume("A", ScentFamily.CITRUS), perfume("B", ScentFamily.FRESH));
		when(repo.findByOwnerIdAndStatus(1L, PerfumeStatus.OWNED)).thenReturn(owned);

		String monday = serviceOn("2026-07-13T10:00:00Z").suggest(1L).perfume().name();
		String mondayAgain = serviceOn("2026-07-13T18:00:00Z").suggest(1L).perfume().name();
		String tuesday = serviceOn("2026-07-14T10:00:00Z").suggest(1L).perfume().name();

		assertThat(mondayAgain).isEqualTo(monday);
		assertThat(tuesday).isNotEqualTo(monday);
	}

	@Test
	void fallsBackToSeasonWhenWeatherUnavailable() {
		when(weather.currentWeather()).thenReturn(Optional.empty());
		when(repo.findByOwnerIdAndStatus(1L, PerfumeStatus.OWNED)).thenReturn(List.of(
				perfume("Holz", ScentFamily.WOODY),
				perfume("Zitrone", ScentFamily.CITRUS)));

		SuggestionResponse result = serviceOn("2026-07-15T10:00:00Z").suggest(1L);

		// Juli -> warme Jahreszeit -> leichter Duft
		assertThat(result.perfume().name()).isEqualTo("Zitrone");
		assertThat(result.temperatureC()).isNull();
	}

	@Test
	void emptyCollectionGivesHint() {
		when(weather.currentWeather()).thenReturn(Optional.empty());
		when(repo.findByOwnerIdAndStatus(1L, PerfumeStatus.OWNED)).thenReturn(List.of());

		SuggestionResponse result = serviceOn("2026-10-12T10:00:00Z").suggest(1L);

		assertThat(result.perfume()).isNull();
		assertThat(result.reason()).contains("Sammlung");
	}
}
