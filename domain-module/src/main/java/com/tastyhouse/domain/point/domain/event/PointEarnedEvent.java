package com.tastyhouse.domain.point.domain.event;

import java.time.LocalDateTime;

import com.tastyhouse.domain.member.domain.vo.MemberId;

public record PointEarnedEvent(
    MemberId memberId,
    int pointAmount,
    String reason,
    LocalDateTime earnedAt
) {}
