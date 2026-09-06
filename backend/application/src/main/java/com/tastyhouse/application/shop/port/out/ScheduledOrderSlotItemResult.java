package com.tastyhouse.application.shop.port.out;

import java.time.LocalDateTime;

public record ScheduledOrderSlotItemResult(
    LocalDateTime startAt,
    LocalDateTime endAt,
    String label,
    String dayLabel
) {
}
