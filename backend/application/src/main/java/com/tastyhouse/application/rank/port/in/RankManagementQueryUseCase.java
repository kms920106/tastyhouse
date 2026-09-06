package com.tastyhouse.application.rank.port.in;

import com.tastyhouse.application.shared.marker.AdminApp;
import java.util.List;

import com.tastyhouse.application.rank.port.out.MemberRankResult;
import com.tastyhouse.application.rank.port.out.RankPeriodResult;
import com.tastyhouse.application.rank.port.out.RankPrizeManagementResult;

@AdminApp
public interface RankManagementQueryUseCase {

    List<MemberRankResult> getMemberRankList(String type, int limit);

    List<RankPeriodResult> getPeriods();

    RankPeriodResult getPeriod(Long id);

    List<RankPrizeManagementResult> getPrizesByPeriod(Long periodId);

    RankPrizeManagementResult getPrize(Long prizeId);
}
