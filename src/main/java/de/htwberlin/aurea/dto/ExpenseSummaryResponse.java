package de.htwberlin.aurea.dto;

import java.math.BigDecimal;

public record ExpenseSummaryResponse(
        int ownedCount,
        int wishlistCount,
        BigDecimal totalValueEur,
        BigDecimal averagePriceEur
) {
}