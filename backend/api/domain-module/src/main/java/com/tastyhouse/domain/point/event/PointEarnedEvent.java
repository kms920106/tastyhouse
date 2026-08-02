package com.tastyhouse.domain.point.event;

import java.time.LocalDateTime;

import com.tastyhouse.domain.member.vo.MemberId;

public record PointEarnedEvent(
    MemberId memberId,
    int pointAmount,
    String reason,
    LocalDateTime earnedAt
) {}
