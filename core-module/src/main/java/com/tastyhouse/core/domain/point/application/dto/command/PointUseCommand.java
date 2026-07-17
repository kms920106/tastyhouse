package com.tastyhouse.core.domain.point.application.dto.command;

import com.tastyhouse.core.domain.member.domain.vo.MemberId;

public record PointUseCommand(
    MemberId memberId,
    int pointAmount
) {

    public static PointUseCommand of(MemberId memberId, int pointAmount) {
        return new PointUseCommand(memberId, pointAmount);
    }
}
