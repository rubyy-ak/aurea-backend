package de.htwberlin.aurea.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PerfumeTest {

	@Test
	void newPerfumeIsOwnedByDefault() {
		Perfume p = new Perfume(new AppUser("x", "hash"));

		assertThat(p.getStatus()).isEqualTo(PerfumeStatus.OWNED);
		assertThat(p.getCreatedAt()).isNotNull();
	}

	@Test
	void familiesAreClassifiedForWeather() {
		assertThat(ScentFamily.CITRUS.isLight()).isTrue();
		assertThat(ScentFamily.ORIENTAL.isLight()).isFalse();
	}
}
