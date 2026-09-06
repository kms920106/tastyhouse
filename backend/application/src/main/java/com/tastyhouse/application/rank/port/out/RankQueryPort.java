package com.tastyhouse.application.rank.port.out;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.tastyhouse.domain.rank.model.RankType;

public interface RankQueryPort {

    Optional<RankDurationResult> findActiveDuration();

    List<RankPrizeResult> findActivePrizes();

    List<MemberRankResult> findMemberRanks(RankType rankType, LocalDate baseDate, int limit);

    Optional<MemberRankResult> findMemberRank(Long memberId, RankType rankType, LocalDate baseDate);
}
