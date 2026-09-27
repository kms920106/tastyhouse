package com.tastyhouse.application.rank.port.out.write;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MemberReviewRankStatePort {
    Optional<MemberReviewRankState> findLatestByMemberIdAndRankType(Long memberId, String rankType);

    void saveAll(List<MemberReviewRankState> states);

    void deleteByRankTypeAndBaseDate(String rankType, LocalDate baseDate);
}
