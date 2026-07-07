package com.tastyhouse.core.domain.rank.domain.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.tastyhouse.core.domain.rank.domain.model.MemberReviewRank;
import com.tastyhouse.core.domain.rank.domain.model.RankType;
import com.tastyhouse.core.domain.rank.application.dto.result.MemberRankResult;

public interface MemberReviewRankRepository {

    List<MemberRankResult> findMemberRankList(RankType rankType, LocalDate baseDate, int limit);

    MemberRankResult findMemberRank(Long memberId, RankType rankType, LocalDate baseDate);

    Optional<MemberReviewRank> findLatestByMemberIdAndRankType(Long memberId, RankType rankType);

    void saveAll(List<MemberReviewRank> ranks);

    void deleteByRankTypeAndBaseDate(RankType rankType, LocalDate baseDate);
}
