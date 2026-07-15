package com.tastyhouse.core.domain.point.application.dto.command;

import com.tastyhouse.core.domain.member.domain.vo.MemberId;

public record UsePointCommand(
    MemberId memberId,
    int pointAmount
) {

    public static UsePointCommand of(MemberId memberId, int pointAmount) {
        return new UsePointCommand(memberId, pointAmount);
    }
}
