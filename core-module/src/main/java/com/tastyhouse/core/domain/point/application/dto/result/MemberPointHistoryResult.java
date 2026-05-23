package com.tastyhouse.core.domain.point.application.dto.result;

import com.tastyhouse.core.domain.point.domain.model.MemberPointHistory;
import com.tastyhouse.core.domain.point.domain.model.PointType;

import java.time.LocalDateTime;

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
