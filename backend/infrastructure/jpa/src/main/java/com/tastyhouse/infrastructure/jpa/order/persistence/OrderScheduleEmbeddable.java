package com.tastyhouse.infrastructure.jpa.order.persistence;

import java.time.LocalDateTime;

import jakarta.persistence.Embeddable;

@Embeddable
public record OrderScheduleEmbeddable(LocalDateTime scheduledAt, LocalDateTime scheduledSlotEndAt) {
}
