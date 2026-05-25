package com.tastyhouse.core.domain.order.domain.event;

import java.time.LocalDateTime;

public record OrderConfirmedEvent(
    Long orderId,
    LocalDateTime confirmedAt
) {
}
