package com.tastyhouse.core.domain.point.domain.event;

import java.time.LocalDateTime;

public record PointUsedEvent(
    Long memberId,
    int pointAmount,
    LocalDateTime usedAt
) {}
