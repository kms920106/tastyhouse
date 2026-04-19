package com.tastyhouse.core.repository.rank;

import com.tastyhouse.core.entity.rank.MemberReviewRank;
import com.tastyhouse.core.entity.rank.RankType;
import com.tastyhouse.core.entity.rank.dto.MemberRankDto;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MemberReviewRankRepository {

    List<MemberRankDto> findMemberRankList(RankType rankType, LocalDate baseDate, int limit);

    MemberRankDto findMemberRank(Long memberId, RankType rankType, LocalDate baseDate);

    Optional<MemberReviewRank> findLatestByMemberIdAndRankType(Long memberId, RankType rankType);

    void deleteByRankTypeAndBaseDate(RankType rankType, LocalDate baseDate);
}
