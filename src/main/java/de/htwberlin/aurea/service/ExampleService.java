package de.htwberlin.aurea.service;

import de.htwberlin.aurea.dto.PerfumeResponse;
import de.htwberlin.aurea.entity.Concentration;
import de.htwberlin.aurea.entity.PerfumeStatus;
import de.htwberlin.aurea.entity.ScentFamily;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/** Feste Beispiel-Parfüms, z. B. für die Login-Seite und als öffentliche Demo der API. */
@Service
public class ExampleService {

	private static final Instant CREATED = Instant.parse("2026-10-01T10:00:00Z");

	private static final List<PerfumeResponse> EXAMPLES = List.of(
			new PerfumeResponse(1L, "Baccarat Rouge 540", "Maison Francis Kurkdjian", Concentration.EAU_DE_PARFUM,
					ScentFamily.ORIENTAL, "Safran, Jasmin", "Amberholz, Ambra", "Tannenharz, Zedernholz",
					70, new BigDecimal("325.00"), 5, PerfumeStatus.OWNED, true, CREATED),
			new PerfumeResponse(2L, "Neroli Portofino", "Tom Ford", Concentration.EAU_DE_PARFUM,
					ScentFamily.CITRUS, "Bergamotte, Zitrone", "Neroli, Orangenblüte", "Ambra",
					50, new BigDecimal("280.00"), 4, PerfumeStatus.OWNED, false, CREATED),
			new PerfumeResponse(3L, "Santal 33", "Le Labo", Concentration.EAU_DE_PARFUM,
					ScentFamily.WOODY, "Kardamom, Veilchen", "Iris", "Sandelholz, Leder",
					50, new BigDecimal("245.00"), 4, PerfumeStatus.OWNED, false, CREATED),
			new PerfumeResponse(4L, "Lost Cherry", "Tom Ford", Concentration.EAU_DE_PARFUM,
					ScentFamily.GOURMAND, "Schwarzkirsche", "Türkische Rose", "Tonkabohne, Vanille",
					50, new BigDecimal("390.00"), null, PerfumeStatus.WISHLIST, false, CREATED));

	public List<PerfumeResponse> examples() {
		return EXAMPLES;
	}
}
