package com.tastyhouse.core.domain.point.domain.event;

import java.time.LocalDateTime;

public record PointRefundedEvent(
    Long memberId,
    int pointAmount,
    LocalDateTime refundedAt
) {}
