package com.tastyhouse.core.domain.point.application.dto.command;

import com.tastyhouse.core.domain.member.domain.vo.MemberId;

public record PointDeductCommand(
    MemberId memberId,
    int pointAmount,
    String reason
) {

    public static PointDeductCommand of(MemberId memberId, int pointAmount, String reason) {
        return new PointDeductCommand(memberId, pointAmount, reason);
    }
}
