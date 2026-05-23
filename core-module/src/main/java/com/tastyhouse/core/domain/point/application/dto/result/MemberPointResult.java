package com.tastyhouse.core.domain.point.application.dto.result;

import com.tastyhouse.core.domain.point.domain.model.MemberPoint;

public record MemberPointResult(
    Long memberId,
    Integer availablePoints,
    Integer expiredThisMonth
) {
    public static MemberPointResult from(MemberPoint memberPoint) {
        return new MemberPointResult(
            memberPoint.getMemberId(),
            memberPoint.getAvailablePoints(),
            memberPoint.getExpiredThisMonth()
        );
    }
}
