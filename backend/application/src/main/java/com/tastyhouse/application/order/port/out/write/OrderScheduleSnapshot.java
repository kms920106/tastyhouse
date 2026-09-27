package com.tastyhouse.application.order.port.out.write;

import java.time.LocalDateTime;

public record OrderScheduleSnapshot(
    LocalDateTime scheduledAt,
    LocalDateTime scheduledSlotEndAt
) {
}
