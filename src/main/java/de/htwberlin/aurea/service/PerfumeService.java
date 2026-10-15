package de.htwberlin.aurea.service;

import de.htwberlin.aurea.dto.ExpenseSummaryResponse;
import de.htwberlin.aurea.dto.PerfumeRequest;
import de.htwberlin.aurea.dto.PerfumeResponse;
import de.htwberlin.aurea.entity.Perfume;
import de.htwberlin.aurea.entity.PerfumeStatus;
import de.htwberlin.aurea.entity.ScentFamily;
import de.htwberlin.aurea.exception.ConflictException;
import de.htwberlin.aurea.exception.NotFoundException;
import de.htwberlin.aurea.repository.AppUserRepository;
import de.htwberlin.aurea.repository.PerfumeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;
import java.util.Objects;

@Service
public class PerfumeService {

	private final PerfumeRepository perfumes;
	private final AppUserRepository users;

	public PerfumeService(PerfumeRepository perfumes, AppUserRepository users) {
		this.perfumes = perfumes;
		this.users = users;
	}

	/** Filterkriterien der Listenansicht; alle optional. */
	public record Filter(PerfumeStatus status, ScentFamily family, String query, Boolean favorite, String sort) {
	}

	@Transactional(readOnly = true)
	public List<PerfumeResponse> list(Long userId, Filter filter) {
		Stream<Perfume> stream = perfumes.findByOwnerIdOrderByCreatedAtDesc(userId).stream();
		if (filter.status() != null) {
			stream = stream.filter(p -> p.getStatus() == filter.status());
		}
		if (filter.family() != null) {
			stream = stream.filter(p -> p.getFamily() == filter.family());
		}
		if (Boolean.TRUE.equals(filter.favorite())) {
			stream = stream.filter(Perfume::isFavorite);
		}
		if (filter.query() != null && !filter.query().isBlank()) {
			String q = filter.query().trim().toLowerCase(Locale.ROOT);
			stream = stream.filter(p -> matches(p, q));
		}
		return stream.sorted(comparator(filter.sort())).map(PerfumeResponse::from).toList();
	}

	@Transactional(readOnly = true)
	public PerfumeResponse get(Long userId, Long id) {
		return PerfumeResponse.from(find(userId, id));
	}

	@Transactional
	public PerfumeResponse create(Long userId, PerfumeRequest request) {
		Perfume perfume = new Perfume(users.getReferenceById(userId));
		apply(perfume, request);
		return PerfumeResponse.from(perfumes.save(perfume));
	}

	@Transactional
	public PerfumeResponse update(Long userId, Long id, PerfumeRequest request) {
		Perfume perfume = find(userId, id);
		apply(perfume, request);
		return PerfumeResponse.from(perfume);
	}

	@Transactional
	public void delete(Long userId, Long id) {
		perfumes.delete(find(userId, id));
	}

	/** Verschiebt ein Parfüm von der Wunschliste in die Sammlung. */
	@Transactional
	public PerfumeResponse purchase(Long userId, Long id) {
		Perfume perfume = find(userId, id);
		if (perfume.getStatus() != PerfumeStatus.WISHLIST) {
			throw new ConflictException("Dieses Parfüm ist bereits in deiner Sammlung.");
		}
		perfume.setStatus(PerfumeStatus.OWNED);
		return PerfumeResponse.from(perfume);
	}

	private Perfume find(Long userId, Long id) {
		// Fremde Parfüms sind für den Nutzer "nicht vorhanden" – kein Hinweis auf ihre Existenz.
		return perfumes.findByIdAndOwnerId(id, userId)
				.orElseThrow(() -> new NotFoundException("Parfüm nicht gefunden."));
	}

	private static void apply(Perfume perfume, PerfumeRequest r) {
		perfume.setName(r.name().trim());
		perfume.setBrand(r.brand().trim());
		perfume.setConcentration(r.concentration());
		perfume.setFamily(r.family());
		perfume.setTopNotes(blankToNull(r.topNotes()));
		perfume.setHeartNotes(blankToNull(r.heartNotes()));
		perfume.setBaseNotes(blankToNull(r.baseNotes()));
		perfume.setSizeMl(r.sizeMl());
		perfume.setPriceEur(r.priceEur());
		perfume.setRating(r.rating());
		perfume.setStatus(r.status() != null ? r.status() : PerfumeStatus.OWNED);
		perfume.setFavorite(Boolean.TRUE.equals(r.favorite()));
	}

	private static String blankToNull(String s) {
		return s == null || s.isBlank() ? null : s.trim();
	}

	private static boolean matches(Perfume p, String q) {
		return Stream.of(p.getName(), p.getBrand(), p.getTopNotes(), p.getHeartNotes(), p.getBaseNotes())
				.anyMatch(s -> s != null && s.toLowerCase(Locale.ROOT).contains(q));
	}

	private static Comparator<Perfume> comparator(String sort) {
		if (sort == null) {
			return (a, b) -> 0; // Repository liefert bereits "neueste zuerst".
		}
		return switch (sort) {
			case "name" -> Comparator.comparing((Perfume p) -> p.getBrand().toLowerCase(Locale.ROOT))
					.thenComparing(p -> p.getName().toLowerCase(Locale.ROOT));
			case "rating" -> Comparator.comparing(Perfume::getRating,
					Comparator.nullsLast(Comparator.reverseOrder()));
			case "price" -> Comparator.comparing(Perfume::getPriceEur,
					Comparator.nullsLast(Comparator.reverseOrder()));
			default -> (a, b) -> 0;
		};
	}
	@Transactional(readOnly = true)
	public ExpenseSummaryResponse summary(Long userId) {
		List<Perfume> all = perfumes.findByOwnerIdOrderByCreatedAtDesc(userId);

		List<Perfume> owned = all.stream()
				.filter(p -> p.getStatus() == PerfumeStatus.OWNED)
				.toList();

		int wishlistCount = (int) all.stream()
				.filter(p -> p.getStatus() == PerfumeStatus.WISHLIST)
				.count();

		List<BigDecimal> prices = owned.stream()
				.map(Perfume::getPriceEur)
				.filter(java.util.Objects::nonNull)
				.toList();

		BigDecimal total = prices.stream()
				.reduce(BigDecimal.ZERO, BigDecimal::add);

		BigDecimal average = prices.isEmpty()
				? BigDecimal.ZERO
				: total.divide(BigDecimal.valueOf(prices.size()), 2, RoundingMode.HALF_UP);

		return new ExpenseSummaryResponse(
				owned.size(),
				wishlistCount,
				total,
				average
		);
	}
}
