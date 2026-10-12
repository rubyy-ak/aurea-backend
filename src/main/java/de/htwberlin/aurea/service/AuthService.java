package de.htwberlin.aurea.service;

import de.htwberlin.aurea.dto.AuthResponse;
import de.htwberlin.aurea.dto.LoginRequest;
import de.htwberlin.aurea.dto.RegisterRequest;
import de.htwberlin.aurea.entity.AppUser;
import de.htwberlin.aurea.entity.AuthToken;
import de.htwberlin.aurea.exception.ConflictException;
import de.htwberlin.aurea.exception.UnauthorizedException;
import de.htwberlin.aurea.repository.AppUserRepository;
import de.htwberlin.aurea.repository.AuthTokenRepository;
import de.htwberlin.aurea.security.AuthUser;
import de.htwberlin.aurea.security.TokenHasher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

@Service
public class AuthService {

	private final AppUserRepository users;
	private final AuthTokenRepository tokens;
	private final PasswordEncoder passwordEncoder;
	private final Duration tokenValidity;

	public AuthService(AppUserRepository users, AuthTokenRepository tokens, PasswordEncoder passwordEncoder,
			@Value("${app.auth.token-validity-days:7}") long tokenValidityDays) {
		this.users = users;
		this.tokens = tokens;
		this.passwordEncoder = passwordEncoder;
		this.tokenValidity = Duration.ofDays(tokenValidityDays);
	}

	@Transactional
	public AuthResponse register(RegisterRequest request) {
		String username = request.username().trim();
		if (users.existsByUsernameIgnoreCase(username)) {
			throw new ConflictException("Dieser Benutzername ist bereits vergeben.");
		}
		AppUser user = users.save(new AppUser(username, passwordEncoder.encode(request.password())));
		return issueToken(user);
	}

	@Transactional
	public AuthResponse login(LoginRequest request) {
		AppUser user = users.findByUsernameIgnoreCase(request.username().trim())
				.filter(u -> passwordEncoder.matches(request.password(), u.getPasswordHash()))
				.orElseThrow(() -> new UnauthorizedException("Benutzername oder Passwort ist falsch."));
		return issueToken(user);
	}

	@Transactional
	public void logout(String token) {
		tokens.deleteById(TokenHasher.hash(token));
	}

	@Transactional
	public Optional<AuthUser> resolveToken(String token) {
		Optional<AuthToken> stored = tokens.findById(TokenHasher.hash(token));
		if (stored.isPresent() && stored.get().isExpired()) {
			tokens.delete(stored.get());
			return Optional.empty();
		}
		return stored.map(t -> new AuthUser(t.getUser().getId(), t.getUser().getUsername()));
	}

	private AuthResponse issueToken(AppUser user) {
		String token = TokenHasher.newToken();
		tokens.save(new AuthToken(TokenHasher.hash(token), user, Instant.now().plus(tokenValidity)));
		return new AuthResponse(token, user.getUsername());
	}
}
