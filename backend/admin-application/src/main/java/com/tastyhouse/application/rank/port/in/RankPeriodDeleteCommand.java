package com.tastyhouse.application.rank.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record RankPeriodDeleteCommand(Long rankPeriodId) {

    public RankPeriodDeleteCommand {
        if (rankPeriodId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }

    public static RankPeriodDeleteCommand of(Long rankPeriodId) {
        return new RankPeriodDeleteCommand(rankPeriodId);
    }
}
