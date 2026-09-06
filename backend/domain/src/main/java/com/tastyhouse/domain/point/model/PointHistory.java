package com.tastyhouse.domain.point.model;

import java.time.LocalDateTime;

import com.tastyhouse.domain.member.vo.MemberId;

public class PointHistory {
    private final Long id;
    private final MemberId memberId;
    private final PointType pointType;
    private final Integer pointAmount;
    private final String reason;
    private final LocalDateTime createdAt;

    private PointHistory(
        Long id,
        MemberId memberId,
        PointType pointType,
        Integer pointAmount,
        String reason,
        LocalDateTime createdAt
    ) {
        this.id = id;
        this.memberId = memberId;
        this.pointType = pointType;
        this.pointAmount = pointAmount;
        this.reason = reason;
        this.createdAt = createdAt;
    }

    public static PointHistory of(MemberId memberId, PointType pointType, Integer pointAmount, String reason) {
        return new PointHistory(null, memberId, pointType, pointAmount, reason, null);
    }

    public static PointHistory reconstitute(
        Long id,
        MemberId memberId,
        PointType pointType,
        Integer pointAmount,
        String reason,
        LocalDateTime createdAt
    ) {
        return new PointHistory(id, memberId, pointType, pointAmount, reason, createdAt);
    }

    public Long getId() {
        return this.id;
    }

    public MemberId getMemberId() {
        return this.memberId;
    }

    public PointType getPointType() {
        return this.pointType;
    }

    public Integer getPointAmount() {
        return this.pointAmount;
    }

    public String getReason() {
        return this.reason;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }
}
