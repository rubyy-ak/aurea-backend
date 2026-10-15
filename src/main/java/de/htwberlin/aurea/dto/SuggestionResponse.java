package de.htwberlin.aurea.dto;

public record SuggestionResponse(
		PerfumeResponse perfume,
		Double temperatureC,
		String weather,
		String reason,
		String status
) {
}