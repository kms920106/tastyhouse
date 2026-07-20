package com.tastyhouse.core.domain.point.application.dto.result;

import com.tastyhouse.core.domain.member.domain.vo.MemberId;
import com.tastyhouse.core.domain.point.domain.model.Point;

public record PointResult(
    MemberId memberId,
    Integer availablePoints,
    Integer expiredThisMonth
) {
    public static PointResult from(Point point) {
        return new PointResult(
            point.getMemberId(),
            point.getAvailablePoints(),
            point.getExpiredThisMonth()
        );
    }
}
