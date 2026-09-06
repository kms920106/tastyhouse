package com.tastyhouse.domain.order.vo;

import java.time.LocalDateTime;

public record OrderSchedule(
    LocalDateTime scheduledAt,
    LocalDateTime scheduledSlotEndAt
) {
    public static OrderSchedule of(LocalDateTime scheduledAt, LocalDateTime scheduledSlotEndAt) {
        return new OrderSchedule(scheduledAt, scheduledSlotEndAt);
    }

    public static OrderSchedule none() {
        return new OrderSchedule(null, null);
    }

    public boolean isPresent() {
        return this.scheduledAt != null;
    }
}
