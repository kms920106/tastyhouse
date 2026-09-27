package com.tastyhouse.infrastructure.order.persistence;

import java.time.LocalDateTime;

import jakarta.persistence.Embeddable;

@Embeddable
public record OrderScheduleEmbeddable(LocalDateTime scheduledAt, LocalDateTime scheduledSlotEndAt) {
}
