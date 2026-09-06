package com.tastyhouse.application.rank.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record RankPrizeUpdateCommand(
    Long rankPrizeId,
    Integer prizeRank,
    String name,
    String brand,
    Long imageFileId
) {
    public RankPrizeUpdateCommand {
        if (rankPrizeId == null || prizeRank == null || name == null || brand == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
