package de.htwberlin.aurea.repository;

import de.htwberlin.aurea.entity.AuthToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthTokenRepository extends JpaRepository<AuthToken, String> {
}
