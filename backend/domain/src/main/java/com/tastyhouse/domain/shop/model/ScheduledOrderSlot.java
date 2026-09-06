package com.tastyhouse.domain.shop.model;

import java.time.LocalDateTime;
import com.tastyhouse.domain.shared.model.OrderMethod;

public record ScheduledOrderSlot(
    LocalDateTime startAt,
    LocalDateTime endAt
) {
    public static ScheduledOrderSlot range(LocalDateTime startAt) {
        return new ScheduledOrderSlot(startAt, startAt.plusMinutes(ScheduledOrderPolicy.SLOT_UNIT_MINUTES));
    }

    public static ScheduledOrderSlot instant(LocalDateTime startAt) {
        return new ScheduledOrderSlot(startAt, startAt);
    }

    public static ScheduledOrderSlot of(OrderMethod orderMethod, LocalDateTime startAt) {
        return ScheduledOrderPolicy.isRangeSlot(orderMethod) ? range(startAt) : instant(startAt);
    }

    public boolean matches(LocalDateTime scheduledAt) {
        return this.startAt.equals(scheduledAt);
    }
}
