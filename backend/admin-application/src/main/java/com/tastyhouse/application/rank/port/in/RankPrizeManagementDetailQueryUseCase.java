package com.tastyhouse.application.rank.port.in;

import com.tastyhouse.application.rank.port.out.RankPrizeManagementResult;

public interface RankPrizeManagementDetailQueryUseCase {

    RankPrizeManagementResult getPrize(Long prizeId);
}
