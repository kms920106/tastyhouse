package com.tastyhouse.application.rank.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record RankPrizeDeleteCommand(Long rankPrizeId) {
    public RankPrizeDeleteCommand {
        if (rankPrizeId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static RankPrizeDeleteCommand of(Long rankPrizeId) {
        return new RankPrizeDeleteCommand(rankPrizeId);
    }
}
