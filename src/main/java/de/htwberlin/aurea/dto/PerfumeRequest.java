package de.htwberlin.aurea.dto;

import de.htwberlin.aurea.entity.Concentration;
import de.htwberlin.aurea.entity.PerfumeStatus;
import de.htwberlin.aurea.entity.ScentFamily;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record PerfumeRequest(
		@NotBlank(message = "Bitte einen Namen angeben.")
		@Size(max = 100, message = "Höchstens 100 Zeichen.")
		String name,

		@NotBlank(message = "Bitte eine Marke angeben.")
		@Size(max = 100, message = "Höchstens 100 Zeichen.")
		String brand,

		@NotNull(message = "Bitte eine Konzentration wählen.")
		Concentration concentration,

		@NotNull(message = "Bitte eine Duftfamilie wählen.")
		ScentFamily family,

		@Size(max = 255, message = "Höchstens 255 Zeichen.") String topNotes,
		@Size(max = 255, message = "Höchstens 255 Zeichen.") String heartNotes,
		@Size(max = 255, message = "Höchstens 255 Zeichen.") String baseNotes,

		@Min(value = 1, message = "Mindestens 1 ml.")
		@Max(value = 1000, message = "Höchstens 1000 ml.")
		Integer sizeMl,

		@DecimalMin(value = "0.00", message = "Der Preis darf nicht negativ sein.")
		@DecimalMax(value = "99999.99", message = "Der Preis ist zu hoch.")
		@Digits(integer = 5, fraction = 2, message = "Höchstens zwei Nachkommastellen.")
		BigDecimal priceEur,

		@Min(value = 1, message = "Bewertung von 1 bis 5.")
		@Max(value = 5, message = "Bewertung von 1 bis 5.")
		Integer rating,

		PerfumeStatus status,

		Boolean favorite
) {
}
