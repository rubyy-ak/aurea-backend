package de.htwberlin.aurea.exception;

import java.util.Map;

/** Einheitliches Fehlerformat für das Frontend. */
public record ApiError(int status, String message, Map<String, String> fieldErrors) {

	public ApiError(int status, String message) {
		this(status, message, Map.of());
	}
}
