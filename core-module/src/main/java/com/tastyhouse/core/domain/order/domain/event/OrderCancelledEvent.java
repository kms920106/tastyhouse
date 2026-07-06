package com.tastyhouse.core.domain.order.domain.event;

import java.time.LocalDateTime;

import com.tastyhouse.core.domain.order.domain.vo.OrderId;

public record OrderCancelledEvent(
    OrderId orderId,
    Long memberId,
    Integer usedPoint,
    Integer earnedPoint,
    String cancelReason,
    LocalDateTime cancelledAt
) {
}
