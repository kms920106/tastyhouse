package com.tastyhouse.webapi.member.service;

import com.tastyhouse.core.domain.member.domain.model.MemberGrade;
import com.tastyhouse.core.domain.rank.application.RankQueryService;
import com.tastyhouse.core.domain.rank.domain.model.MemberReviewRank;
import com.tastyhouse.core.domain.rank.domain.model.RankType;
import com.tastyhouse.webapi.member.response.MyGradeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberGradeService {

    private final RankQueryService rankQueryService;

    @Transactional(readOnly = true)
    public MyGradeResponse getMyGrade(Long memberId) {
        int currentReviewCount = rankQueryService.findLatestByMemberIdAndRankType(memberId, RankType.ALL).map(MemberReviewRank::getReviewCount).orElse(0);

        MemberGrade currentGrade = MemberGrade.fromReviewCount(currentReviewCount);
        MemberGrade nextGrade = currentGrade.isHigherThanOrEqual(MemberGrade.TEHA) ? null : MemberGrade.fromLevel(currentGrade.getLevel() + 1);

        int reviewsNeeded = 0;
        if (nextGrade != null) {
            reviewsNeeded = nextGrade.getMinReviewCount() - currentReviewCount;
        }

        return MyGradeResponse.from(
            currentGrade,
            currentGrade.getDisplayName(),
            nextGrade,
            nextGrade != null ? nextGrade.getDisplayName() : null,
            currentReviewCount,
            reviewsNeeded
        );
    }
}
