package de.htwberlin.aurea.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * Login-Sitzung. Gespeichert wird nur der SHA-256-Hash des Tokens,
 * damit ein Datenbank-Leak keine gültigen Sitzungen preisgibt.
 */
@Entity
@Table(name = "auth_token")
public class AuthToken {

	@Id
	@Column(length = 64)
	private String tokenHash;

	@ManyToOne(fetch = FetchType.EAGER, optional = false)
	@JoinColumn(name = "user_id")
	private AppUser user;

	@Column(nullable = false)
	private Instant expiresAt;

	protected AuthToken() {
	}

	public AuthToken(String tokenHash, AppUser user, Instant expiresAt) {
		this.tokenHash = tokenHash;
		this.user = user;
		this.expiresAt = expiresAt;
	}

	public String getTokenHash() { return tokenHash; }
	public AppUser getUser() { return user; }
	public Instant getExpiresAt() { return expiresAt; }

	public boolean isExpired() {
		return Instant.now().isAfter(expiresAt);
	}
}
