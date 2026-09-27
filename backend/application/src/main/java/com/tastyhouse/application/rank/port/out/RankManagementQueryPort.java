package com.tastyhouse.application.rank.port.out;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RankManagementQueryPort {

    List<MemberRankResult> findMemberRanks(String rankType, LocalDate baseDate, int limit);

    List<RankPeriodResult> findAllPeriods();

    Optional<RankPeriodResult> findPeriodById(Long id);

    List<RankPrizeManagementResult> findPrizesByPeriodId(Long periodId);

    Optional<RankPrizeManagementResult> findPrizeById(Long id);
}
