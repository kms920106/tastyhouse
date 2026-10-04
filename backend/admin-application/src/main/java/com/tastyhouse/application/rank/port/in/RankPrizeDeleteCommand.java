package com.tastyhouse.application.rank.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record RankPrizeDeleteCommand(Long rankPrizeId) {

    public RankPrizeDeleteCommand {
        if (rankPrizeId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }

    public static RankPrizeDeleteCommand of(Long rankPrizeId) {
        return new RankPrizeDeleteCommand(rankPrizeId);
    }
}
