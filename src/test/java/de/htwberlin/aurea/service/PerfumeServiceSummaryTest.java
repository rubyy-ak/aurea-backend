package de.htwberlin.aurea.service;

import de.htwberlin.aurea.dto.ExpenseSummaryResponse;
import de.htwberlin.aurea.entity.Perfume;
import de.htwberlin.aurea.entity.PerfumeStatus;
import de.htwberlin.aurea.repository.AppUserRepository;
import de.htwberlin.aurea.repository.PerfumeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PerfumeServiceSummaryTest {

    @Mock
    private PerfumeRepository perfumes;
    @Mock
    private AppUserRepository users;
    @InjectMocks
    private PerfumeService service;

    private static Perfume perfume(PerfumeStatus status, String price) {
        Perfume p = new Perfume(null);
        p.setStatus(status);
        p.setPriceEur(price == null ? null : new BigDecimal(price));
        return p;
    }

    @Test
    void emptyCollectionGivesZeros() {
        when(perfumes.findByOwnerIdOrderByCreatedAtDesc(1L)).thenReturn(List.of());

        ExpenseSummaryResponse r = service.summary(1L);

        assertThat(r.ownedCount()).isZero();
        assertThat(r.totalValueEur()).isEqualByComparingTo("0");
        assertThat(r.averagePriceEur()).isEqualByComparingTo("0");
    }

    @Test
    void sumsOnlyOwnedPerfumesAndIgnoresWishlist() {
        when(perfumes.findByOwnerIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(
                perfume(PerfumeStatus.OWNED, "100.00"),
                perfume(PerfumeStatus.OWNED, "50.00"),
                perfume(PerfumeStatus.WISHLIST, "200.00")));

        ExpenseSummaryResponse r = service.summary(1L);

        assertThat(r.ownedCount()).isEqualTo(2);
        assertThat(r.wishlistCount()).isEqualTo(1);
        assertThat(r.totalValueEur()).isEqualByComparingTo("150.00");
        assertThat(r.averagePriceEur()).isEqualByComparingTo("75.00");
    }

    @Test
    void perfumeWithoutPriceCountsAsZeroInTotalButNotInAverage() {
        when(perfumes.findByOwnerIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(
                perfume(PerfumeStatus.OWNED, "80.00"),
                perfume(PerfumeStatus.OWNED, null)));

        ExpenseSummaryResponse r = service.summary(1L);

        assertThat(r.ownedCount()).isEqualTo(2);
        assertThat(r.totalValueEur()).isEqualByComparingTo("80.00");
        assertThat(r.averagePriceEur()).isEqualByComparingTo("80.00");
    }
}