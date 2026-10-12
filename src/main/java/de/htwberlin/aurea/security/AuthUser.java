package de.htwberlin.aurea.security;

/** Der angemeldete Nutzer, wie ihn Controller per {@code @AuthenticationPrincipal} erhalten. */
public record AuthUser(Long id, String username) {
}
