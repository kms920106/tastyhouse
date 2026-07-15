package com.tastyhouse.core.domain.point.application.dto.command;

import com.tastyhouse.core.domain.member.domain.vo.MemberId;

public record RefundPointCommand(
    MemberId memberId,
    int pointAmount
) {

    public static RefundPointCommand of(MemberId memberId, int pointAmount) {
        return new RefundPointCommand(memberId, pointAmount);
    }
}
