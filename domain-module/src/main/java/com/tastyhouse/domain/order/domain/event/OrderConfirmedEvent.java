package com.tastyhouse.domain.order.domain.event;

import java.time.LocalDateTime;

import com.tastyhouse.domain.order.domain.vo.OrderId;

public record OrderConfirmedEvent(
    OrderId orderId,
    LocalDateTime confirmedAt
) {
}
