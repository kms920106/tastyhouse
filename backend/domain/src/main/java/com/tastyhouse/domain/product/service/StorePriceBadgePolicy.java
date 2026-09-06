package com.tastyhouse.domain.product.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import com.tastyhouse.domain.product.model.ProductPrice;

public class StorePriceBadgePolicy {
    private static final double PICKUP_BADGE_COVERAGE_THRESHOLD = 0.8d;

    public boolean shouldExposePickupBadge(
        List<ProductPrice> prices,
        long totalProductCount,
        List<LocalDate> businessDates,
        LocalDateTime now
    ) {
        if (prices == null || prices.isEmpty() || totalProductCount <= 0L) {
            return false;
        }

        boolean anyPickupAboveStore = prices.stream()
            .filter(ProductPrice::hasStoreAndPickupPrice)
            .anyMatch(price -> !price.isPickupPriceWithinStorePrice());
        if (anyPickupAboveStore) {
            return false;
        }

        if (!meetsCoverage(prices, totalProductCount)) {
            return false;
        }

        return hasBusinessDayPassed(prices, businessDates, now);
    }

    private static boolean meetsCoverage(List<ProductPrice> prices, long totalProductCount) {
        long coveredProductCount = prices.stream()
            .filter(ProductPrice::hasStoreAndPickupPrice)
            .map(price -> price.getProductId().value())
            .distinct()
            .count();
        return coveredProductCount >= Math.ceil(totalProductCount * PICKUP_BADGE_COVERAGE_THRESHOLD);
    }

    private static boolean hasBusinessDayPassed(
        List<ProductPrice> prices,
        List<LocalDate> businessDates,
        LocalDateTime now
    ) {
        LocalDateTime latestSetAt = prices.stream()
            .filter(ProductPrice::hasStoreAndPickupPrice)
            .map(ProductPrice::getPickupPriceSetAt)
            .filter(Objects::nonNull)
            .max(LocalDateTime::compareTo)
            .orElse(null);

        if (latestSetAt == null) {
            return true;
        }
        if (businessDates == null || businessDates.isEmpty()) {
            return false;
        }

        LocalDate setDate = latestSetAt.toLocalDate();

        return businessDates.stream()
            .anyMatch(date -> date.isAfter(setDate) && !date.isAfter(now.toLocalDate()));
    }

    public boolean shouldExposeSameAsStorePriceBadge(boolean storePriceVerified) {
        return storePriceVerified;
    }
}
