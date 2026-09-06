package com.tastyhouse.domain.point.event;

import java.time.LocalDateTime;

import com.tastyhouse.domain.member.vo.MemberId;

public record PointUsedEvent(
    MemberId memberId,
    int pointAmount,
    LocalDateTime usedAt
) {}
