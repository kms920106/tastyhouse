package com.tastyhouse.infrastructure.persistence.product.query;

import java.time.LocalDateTime;

public record ProductOptionAvailabilityRow(
    Long optionGroupId,
    Long id,
    String name,
    Integer additionalPrice,
    Boolean soldOut,
    LocalDateTime soldOutUntil,
    Boolean visible,
    Integer sort
) {
}
