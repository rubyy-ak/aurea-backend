package de.htwberlin.aurea.dto;

public record PerfumeSuggestionResponse(
        String type,
        String message,
        PerfumeResponse perfume
) {
}