package com.tastyhouse.application.rank.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record RankPrizeCreateCommand(
    Long rankPeriodId,
    Integer prizeRank,
    String name,
    String brand,
    Long imageFileId
) {
    public RankPrizeCreateCommand {
        if (rankPeriodId == null || prizeRank == null || name == null || brand == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
