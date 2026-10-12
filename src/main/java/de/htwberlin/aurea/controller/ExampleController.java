package de.htwberlin.aurea.controller;

import de.htwberlin.aurea.dto.PerfumeResponse;
import de.htwberlin.aurea.service.ExampleService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Öffentliche Route ohne Login: eine Liste von Beispiel-Parfüms. */
@RestController
public class ExampleController {

	private final ExampleService exampleService;

	public ExampleController(ExampleService exampleService) {
		this.exampleService = exampleService;
	}

	@GetMapping("/api/examples")
	public List<PerfumeResponse> examples() {
		return exampleService.examples();
	}
}
