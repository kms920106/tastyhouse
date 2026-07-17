package com.tastyhouse.core.domain.point.application.dto;

import com.tastyhouse.core.domain.member.domain.vo.MemberId;
import com.tastyhouse.core.domain.point.domain.model.PointType;

public record PointSearchCondition(
    MemberId memberId,
    PointType pointType
) {

    public static PointSearchCondition of(MemberId memberId, PointType pointType) {
        return new PointSearchCondition(memberId, pointType);
    }
}
