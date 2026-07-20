package com.tastyhouse.core.domain.point.application.dto.result;

import java.time.LocalDateTime;

import com.tastyhouse.core.domain.point.domain.model.PointHistory;
import com.tastyhouse.core.domain.point.domain.model.PointType;

public record PointHistoryResult(
    PointType pointType,
    Integer pointAmount,
    String reason,
    LocalDateTime createdAt
) {
    public static PointHistoryResult from(PointHistory history) {
        return new PointHistoryResult(
            history.getPointType(),
            history.getPointAmount(),
            history.getReason(),
            history.getCreatedAt()
        );
    }
}
