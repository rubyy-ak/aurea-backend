package de.htwberlin.aurea.controller;

import de.htwberlin.aurea.dto.AuthResponse;
import de.htwberlin.aurea.dto.LoginRequest;
import de.htwberlin.aurea.dto.RegisterRequest;
import de.htwberlin.aurea.dto.UserResponse;
import de.htwberlin.aurea.security.AuthUser;
import de.htwberlin.aurea.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final AuthService authService;

	public AuthController(AuthService authService) {
		this.authService = authService;
	}

	@PostMapping("/register")
	@ResponseStatus(HttpStatus.CREATED)
	public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
		return authService.register(request);
	}

	@PostMapping("/login")
	public AuthResponse login(@Valid @RequestBody LoginRequest request) {
		return authService.login(request);
	}

	@PostMapping("/logout")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void logout(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
		authService.logout(authorization.substring("Bearer ".length()).trim());
	}

	@GetMapping("/me")
	public UserResponse me(@AuthenticationPrincipal AuthUser user) {
		return new UserResponse(user.id(), user.username());
	}
}
