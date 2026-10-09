package com.tastyhouse.application.rank.port.out;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RankQueryPort {

    Optional<RankDurationResult> findActiveDuration();

    List<RankPrizeResult> findActivePrizes();

    List<MemberRankResult> findMemberRanks(String rankType, LocalDate baseDate, int limit);

    Optional<MemberRankResult> findMemberRank(Long memberId, String rankType, LocalDate baseDate);

    Optional<Integer> findLatestReviewCount(Long memberId, String rankType);
}
