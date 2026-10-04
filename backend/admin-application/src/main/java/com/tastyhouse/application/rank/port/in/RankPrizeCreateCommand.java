package com.tastyhouse.application.rank.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record RankPrizeCreateCommand(
    Long rankPeriodId,
    Integer prizeRank,
    String name,
    String brand,
    Long imageFileId
) {

    public RankPrizeCreateCommand {
        if (rankPeriodId == null || prizeRank == null || name == null || brand == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }
}
