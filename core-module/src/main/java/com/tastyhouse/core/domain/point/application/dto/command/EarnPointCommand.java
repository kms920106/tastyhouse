package com.tastyhouse.core.domain.point.application.dto.command;

import com.tastyhouse.core.domain.member.domain.vo.MemberId;

public record EarnPointCommand(
    MemberId memberId,
    int pointAmount,
    String reason
) {

    public static EarnPointCommand of(MemberId memberId, int pointAmount, String reason) {
        return new EarnPointCommand(memberId, pointAmount, reason);
    }
}
