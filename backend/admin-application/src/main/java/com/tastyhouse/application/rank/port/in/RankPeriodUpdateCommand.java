package com.tastyhouse.application.rank.port.in;

import java.time.LocalDateTime;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record RankPeriodUpdateCommand(
    Long rankPeriodId,
    LocalDateTime startAt,
    LocalDateTime endAt,
    boolean visible
) {

    public RankPeriodUpdateCommand {
        if (rankPeriodId == null || startAt == null || endAt == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }
}
