package com.tastyhouse.application.rank.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record RankPeriodDeleteCommand(Long rankPeriodId) {
    public RankPeriodDeleteCommand {
        if (rankPeriodId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static RankPeriodDeleteCommand of(Long rankPeriodId) {
        return new RankPeriodDeleteCommand(rankPeriodId);
    }
}
