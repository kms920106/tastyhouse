package com.tastyhouse.application.rank.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record RankPrizeUpdateCommand(
    Long rankPrizeId,
    Integer prizeRank,
    String name,
    String brand,
    Long imageFileId
) {

    public RankPrizeUpdateCommand {
        if (rankPrizeId == null || prizeRank == null || name == null || brand == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }
}
