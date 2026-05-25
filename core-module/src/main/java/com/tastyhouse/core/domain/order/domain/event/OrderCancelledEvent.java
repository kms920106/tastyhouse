package com.tastyhouse.core.domain.order.domain.event;

import java.time.LocalDateTime;

public record OrderCancelledEvent(
    Long orderId,
    Long memberId,
    Integer usedPoint,
    Integer earnedPoint,
    String cancelReason,
    LocalDateTime cancelledAt
) {
}
