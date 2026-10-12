package de.htwberlin.aurea.dto;

import de.htwberlin.aurea.entity.Concentration;
import de.htwberlin.aurea.entity.Perfume;
import de.htwberlin.aurea.entity.PerfumeStatus;
import de.htwberlin.aurea.entity.ScentFamily;

import java.math.BigDecimal;
import java.time.Instant;

public record PerfumeResponse(
		Long id,
		String name,
		String brand,
		Concentration concentration,
		ScentFamily family,
		String topNotes,
		String heartNotes,
		String baseNotes,
		Integer sizeMl,
		BigDecimal priceEur,
		Integer rating,
		PerfumeStatus status,
		boolean favorite,
		Instant createdAt
) {

	public static PerfumeResponse from(Perfume p) {
		return new PerfumeResponse(p.getId(), p.getName(), p.getBrand(), p.getConcentration(),
				p.getFamily(), p.getTopNotes(), p.getHeartNotes(), p.getBaseNotes(), p.getSizeMl(),
				p.getPriceEur(), p.getRating(), p.getStatus(), p.isFavorite(), p.getCreatedAt());
	}
}
