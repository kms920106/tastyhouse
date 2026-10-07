package com.tastyhouse.application.rank.port.in;

import java.util.List;

import com.tastyhouse.application.rank.port.out.RankPrizeManagementResult;

public interface RankPrizeManagementListQueryUseCase {

    List<RankPrizeManagementResult> getPrizesByPeriod(Long periodId);
}
