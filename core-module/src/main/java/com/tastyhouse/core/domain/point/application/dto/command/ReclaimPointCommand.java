package com.tastyhouse.core.domain.point.application.dto.command;

import com.tastyhouse.core.domain.member.domain.vo.MemberId;

public record ReclaimPointCommand(
    MemberId memberId,
    int pointAmount
) {

    public static ReclaimPointCommand of(MemberId memberId, int pointAmount) {
        return new ReclaimPointCommand(memberId, pointAmount);
    }
}
