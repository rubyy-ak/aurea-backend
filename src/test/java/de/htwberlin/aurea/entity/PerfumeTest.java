package de.htwberlin.aurea.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class PerfumeTest {

	@Test
	void wearIncrementsCountAndKeepsLatestDate() {
		Perfume p = new Perfume(new AppUser("x", "hash"));

		p.wear(LocalDate.of(2026, 10, 5));
		p.wear(LocalDate.of(2026, 10, 1));

		assertThat(p.getWearCount()).isEqualTo(2);
		assertThat(p.getLastWornOn()).isEqualTo(LocalDate.of(2026, 10, 5));
	}

	@Test
	void familiesAreClassifiedForWeather() {
		assertThat(ScentFamily.CITRUS.isLight()).isTrue();
		assertThat(ScentFamily.ORIENTAL.isLight()).isFalse();
	}
}
