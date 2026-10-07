package com.tastyhouse.application.rank.port.in;

import com.tastyhouse.application.rank.port.out.RankPeriodResult;

public interface RankPeriodManagementDetailQueryUseCase {

    RankPeriodResult getPeriod(Long id);
}
