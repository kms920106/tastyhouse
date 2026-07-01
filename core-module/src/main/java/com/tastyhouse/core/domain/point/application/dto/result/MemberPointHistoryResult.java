package com.tastyhouse.core.domain.point.application.dto.result;

import java.time.LocalDateTime;

import com.tastyhouse.core.domain.point.domain.model.MemberPointHistory;
import com.tastyhouse.core.domain.point.domain.model.PointType;

public record MemberPointHistoryResult(
    PointType pointType,
    Integer pointAmount,
    String reason,
    LocalDateTime createdAt
) {
    public static MemberPointHistoryResult from(MemberPointHistory history) {
        return new MemberPointHistoryResult(
            history.getPointType(),
            history.getPointAmount(),
            history.getReason(),
            history.getCreatedAt()
        );
    }
}
