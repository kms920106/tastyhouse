package com.tastyhouse.domain.rank.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.rank.model.MemberReviewRank;
import com.tastyhouse.domain.rank.model.RankType;

public interface MemberReviewRankRepository {
    Optional<MemberReviewRank> findLatestByMemberIdAndRankType(MemberId memberId, RankType rankType);

    void saveAll(List<MemberReviewRank> ranks);

    void deleteByRankTypeAndBaseDate(RankType rankType, LocalDate baseDate);
}
