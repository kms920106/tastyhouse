package com.tastyhouse.core.domain.point.application.dto.command;

import com.tastyhouse.core.domain.member.domain.vo.MemberId;

public record PointEarnCommand(
    MemberId memberId,
    int pointAmount,
    String reason
) {

    public static PointEarnCommand of(MemberId memberId, int pointAmount, String reason) {
        return new PointEarnCommand(memberId, pointAmount, reason);
    }
}
