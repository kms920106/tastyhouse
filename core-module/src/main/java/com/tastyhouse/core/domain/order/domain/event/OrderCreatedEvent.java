package com.tastyhouse.core.domain.order.domain.event;

import java.time.LocalDateTime;

public record OrderCreatedEvent(
    Long orderId,
    Long memberId,
    Long shopId,
    Integer finalAmount,
    LocalDateTime createdAt
) {
}
