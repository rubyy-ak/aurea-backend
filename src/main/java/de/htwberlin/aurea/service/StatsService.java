package de.htwberlin.aurea.service;

import de.htwberlin.aurea.dto.PerfumeResponse;
import de.htwberlin.aurea.dto.StatsResponse;
import de.htwberlin.aurea.entity.Perfume;
import de.htwberlin.aurea.entity.PerfumeStatus;
import de.htwberlin.aurea.entity.ScentFamily;
import de.htwberlin.aurea.repository.PerfumeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class StatsService {

	private final PerfumeRepository perfumes;

	public StatsService(PerfumeRepository perfumes) {
		this.perfumes = perfumes;
	}

	@Transactional(readOnly = true)
	public StatsResponse stats(Long userId) {
		List<Perfume> all = perfumes.findByOwnerIdOrderByCreatedAtDesc(userId);
		List<Perfume> owned = all.stream().filter(p -> p.getStatus() == PerfumeStatus.OWNED).toList();
		List<Perfume> wishlist = all.stream().filter(p -> p.getStatus() == PerfumeStatus.WISHLIST).toList();

		Map<String, Integer> familyCounts = new LinkedHashMap<>();
		for (ScentFamily family : ScentFamily.values()) {
			int count = (int) owned.stream().filter(p -> p.getFamily() == family).count();
			if (count > 0) {
				familyCounts.put(family.name(), count);
			}
		}

		Double averageRating = owned.stream()
				.map(Perfume::getRating).filter(Objects::nonNull)
				.mapToInt(Integer::intValue).average()
				.stream().mapToObj(avg -> Math.round(avg * 10) / 10.0).findFirst().orElse(null);

		PerfumeResponse mostWorn = owned.stream()
				.filter(p -> p.getWearCount() > 0)
				.max(Comparator.comparingInt(Perfume::getWearCount))
				.map(PerfumeResponse::from).orElse(null);

		return new StatsResponse(
				owned.size(),
				wishlist.size(),
				(int) owned.stream().filter(Perfume::isFavorite).count(),
				sumPrices(owned),
				sumPrices(wishlist),
				owned.stream().map(Perfume::getSizeMl).filter(Objects::nonNull).mapToInt(Integer::intValue).sum(),
				owned.stream().mapToInt(Perfume::getWearCount).sum(),
				averageRating,
				familyCounts,
				mostWorn);
	}

	private static BigDecimal sumPrices(List<Perfume> list) {
		return list.stream().map(Perfume::getPriceEur).filter(Objects::nonNull)
				.reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.HALF_UP);
	}
}
