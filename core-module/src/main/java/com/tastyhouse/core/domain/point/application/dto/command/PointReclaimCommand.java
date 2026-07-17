package com.tastyhouse.core.domain.point.application.dto.command;

import com.tastyhouse.core.domain.member.domain.vo.MemberId;

public record PointReclaimCommand(
    MemberId memberId,
    int pointAmount
) {

    public static PointReclaimCommand of(MemberId memberId, int pointAmount) {
        return new PointReclaimCommand(memberId, pointAmount);
    }
}
