package de.htwberlin.aurea.repository;

import de.htwberlin.aurea.entity.Perfume;
import de.htwberlin.aurea.entity.PerfumeStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PerfumeRepository extends JpaRepository<Perfume, Long> {

	List<Perfume> findByOwnerIdOrderByCreatedAtDesc(Long ownerId);

	List<Perfume> findByOwnerIdAndStatus(Long ownerId, PerfumeStatus status);

	Optional<Perfume> findByIdAndOwnerId(Long id, Long ownerId);
}
