package com.tastyhouse.application.rank.port.out.write;

import java.time.LocalDate;
import java.util.List;

import com.tastyhouse.domain.rank.model.MemberReviewRank;
import com.tastyhouse.domain.rank.model.RankType;

public interface MemberReviewRankPersistencePort {

    void saveAll(List<MemberReviewRank> ranks);

    void deleteByRankTypeAndBaseDate(RankType rankType, LocalDate baseDate);
}
