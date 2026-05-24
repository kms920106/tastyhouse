package com.tastyhouse.core.domain.product.domain.event;

import java.time.LocalDateTime;

public record ProductCreatedEvent(
    Long productId,
    Long placeId,
    LocalDateTime occurredAt
) {
}
