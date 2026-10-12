package de.htwberlin.aurea.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
		@NotBlank(message = "Bitte den Benutzernamen angeben.") String username,
		@NotBlank(message = "Bitte das Passwort angeben.") String password
) {
}
