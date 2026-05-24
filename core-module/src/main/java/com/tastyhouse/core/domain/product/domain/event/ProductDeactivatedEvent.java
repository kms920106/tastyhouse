package com.tastyhouse.core.domain.product.domain.event;

import java.time.LocalDateTime;

public record ProductDeactivatedEvent(
    Long productId,
    Long placeId,
    LocalDateTime occurredAt
) {
}
