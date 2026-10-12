package de.htwberlin.aurea.dto;

import java.math.BigDecimal;
import java.util.Map;

public record StatsResponse(
		int ownedCount,
		int wishlistCount,
		int favoriteCount,
		BigDecimal collectionValueEur,
		BigDecimal wishlistValueEur,
		int totalMl,
		int totalWears,
		Double averageRating,
		Map<String, Integer> familyCounts,
		PerfumeResponse mostWorn
) {
}
