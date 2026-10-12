package de.htwberlin.aurea.controller;

import de.htwberlin.aurea.dto.StatsResponse;
import de.htwberlin.aurea.security.AuthUser;
import de.htwberlin.aurea.service.StatsService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StatsController {

	private final StatsService statsService;

	public StatsController(StatsService statsService) {
		this.statsService = statsService;
	}

	@GetMapping("/api/stats")
	public StatsResponse stats(@AuthenticationPrincipal AuthUser user) {
		return statsService.stats(user.id());
	}
}
