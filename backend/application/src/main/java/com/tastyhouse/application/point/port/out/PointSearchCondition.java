package com.tastyhouse.application.point.port.out;

import com.tastyhouse.domain.point.model.PointType;

public record PointSearchCondition(
    Long memberId,
    PointType pointType
) {

    public static PointSearchCondition of(Long memberId, PointType pointType) {
        return new PointSearchCondition(memberId, pointType);
    }
}
