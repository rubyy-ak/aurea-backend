package de.htwberlin.aurea.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
		@NotBlank(message = "Bitte einen Benutzernamen angeben.")
		@Pattern(regexp = "^[A-Za-z0-9_.@+-]{3,40}$",
				message = "3–40 Zeichen ohne Leerzeichen – z. B. ein Name oder deine E-Mail-Adresse.")
		String username,

		@NotBlank(message = "Bitte ein Passwort angeben.")
		@Size(min = 8, max = 72, message = "Das Passwort muss 8–72 Zeichen lang sein.")
		String password
) {
}
