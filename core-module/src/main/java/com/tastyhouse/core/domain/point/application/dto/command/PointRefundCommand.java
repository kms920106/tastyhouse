package com.tastyhouse.core.domain.point.application.dto.command;

import com.tastyhouse.core.domain.member.domain.vo.MemberId;

public record PointRefundCommand(
    MemberId memberId,
    int pointAmount
) {

    public static PointRefundCommand of(MemberId memberId, int pointAmount) {
        return new PointRefundCommand(memberId, pointAmount);
    }
}
