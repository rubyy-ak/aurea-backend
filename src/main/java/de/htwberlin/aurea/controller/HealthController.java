package de.htwberlin.aurea.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** Öffentlicher Endpunkt für den Render-Healthcheck. */
@RestController
public class HealthController {

	@GetMapping("/api/health")
	public Map<String, String> health() {
		return Map.of("status", "ok", "app", "Auréa");
	}
}
