package com.tastyhouse.application.rank.port.out;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.tastyhouse.domain.rank.model.RankType;
import com.tastyhouse.domain.rank.vo.RankPeriodId;
import com.tastyhouse.domain.rank.vo.RankPrizeId;

public interface RankManagementQueryPort {

    List<MemberRankResult> findMemberRanks(RankType rankType, LocalDate baseDate, int limit);

    List<RankPeriodResult> findAllPeriods();

    Optional<RankPeriodResult> findPeriodById(RankPeriodId id);

    List<RankPrizeManagementResult> findPrizesByPeriodId(RankPeriodId periodId);

    Optional<RankPrizeManagementResult> findPrizeById(RankPrizeId id);
}
