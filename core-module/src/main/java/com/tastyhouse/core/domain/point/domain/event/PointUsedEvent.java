package com.tastyhouse.core.domain.point.domain.event;

import java.time.LocalDateTime;

import com.tastyhouse.core.domain.member.domain.vo.MemberId;

public record PointUsedEvent(
    MemberId memberId,
    int pointAmount,
    LocalDateTime usedAt
) {}
