package com.tastyhouse.domain.product.domain.event;

import java.time.LocalDateTime;

public record ProductSoldOutChangedEvent(
    Long productId,
    Long shopId,
    boolean isSoldOut,
    LocalDateTime occurredAt
) {
}
