package com.tastyhouse.application.rank.port.in;

import java.time.LocalDateTime;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record RankPeriodUpdateCommand(
    Long rankPeriodId,
    LocalDateTime startAt,
    LocalDateTime endAt,
    boolean visible
) {
    public RankPeriodUpdateCommand {
        if (rankPeriodId == null || startAt == null || endAt == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
