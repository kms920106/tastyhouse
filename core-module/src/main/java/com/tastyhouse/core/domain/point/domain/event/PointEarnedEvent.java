package com.tastyhouse.core.domain.point.domain.event;

import java.time.LocalDateTime;

public record PointEarnedEvent(
    Long memberId,
    int pointAmount,
    String reason,
    LocalDateTime earnedAt
) {}
