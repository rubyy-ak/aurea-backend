package de.htwberlin.aurea.controller;

import de.htwberlin.aurea.dto.PerfumeRequest;
import de.htwberlin.aurea.dto.PerfumeResponse;
import de.htwberlin.aurea.dto.SuggestionResponse;
import de.htwberlin.aurea.entity.PerfumeStatus;
import de.htwberlin.aurea.entity.ScentFamily;
import de.htwberlin.aurea.security.AuthUser;
import de.htwberlin.aurea.service.PerfumeService;
import de.htwberlin.aurea.service.SuggestionService;
import de.htwberlin.aurea.weather.CurrentWeather;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/perfumes")
public class PerfumeController {

	private final PerfumeService perfumeService;
	private final SuggestionService suggestionService;

	public PerfumeController(PerfumeService perfumeService, SuggestionService suggestionService) {
		this.perfumeService = perfumeService;
		this.suggestionService = suggestionService;
	}

	@GetMapping
	public List<PerfumeResponse> list(@AuthenticationPrincipal AuthUser user,
			@RequestParam(required = false) PerfumeStatus status,
			@RequestParam(required = false) ScentFamily family,
			@RequestParam(required = false) String q,
			@RequestParam(required = false) Boolean favorite,
			@RequestParam(required = false) String sort) {
		return perfumeService.list(user.id(), new PerfumeService.Filter(status, family, q, favorite, sort));
	}

	@GetMapping("/suggestion")
	public SuggestionResponse suggestion(@AuthenticationPrincipal AuthUser user,
			@RequestParam(required = false) Double temperature,
			@RequestParam(required = false) Integer weatherCode) {
		return suggestionService.suggest(user.id(), CurrentWeather.ofPlausible(temperature, weatherCode));
	}

	@GetMapping("/{id}")
	public PerfumeResponse get(@AuthenticationPrincipal AuthUser user, @PathVariable Long id) {
		return perfumeService.get(user.id(), id);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public PerfumeResponse create(@AuthenticationPrincipal AuthUser user,
			@Valid @RequestBody PerfumeRequest request) {
		return perfumeService.create(user.id(), request);
	}

	@PutMapping("/{id}")
	public PerfumeResponse update(@AuthenticationPrincipal AuthUser user, @PathVariable Long id,
			@Valid @RequestBody PerfumeRequest request) {
		return perfumeService.update(user.id(), id, request);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@AuthenticationPrincipal AuthUser user, @PathVariable Long id) {
		perfumeService.delete(user.id(), id);
	}

	@PostMapping("/{id}/purchase")
	public PerfumeResponse purchase(@AuthenticationPrincipal AuthUser user, @PathVariable Long id) {
		return perfumeService.purchase(user.id(), id);
	}
}
