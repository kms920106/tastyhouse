package com.tastyhouse.application.rank.port.in;

import java.util.List;

import com.tastyhouse.application.rank.port.out.RankPeriodResult;

public interface RankPeriodManagementListQueryUseCase {

    List<RankPeriodResult> getPeriods();
}
