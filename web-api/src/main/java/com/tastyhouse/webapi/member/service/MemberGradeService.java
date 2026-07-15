package com.tastyhouse.webapi.member.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.core.domain.member.domain.model.MemberGrade;
import com.tastyhouse.core.domain.member.domain.vo.MemberId;
import com.tastyhouse.core.domain.rank.domain.model.MemberReviewRank;
import com.tastyhouse.core.domain.rank.domain.model.RankType;
import com.tastyhouse.core.domain.rank.application.RankQueryService;
import com.tastyhouse.webapi.member.response.MyGradeResponse;

@Service
@RequiredArgsConstructor
public class MemberGradeService {

    private final RankQueryService rankQueryService;

    @Transactional(readOnly = true)
    public MyGradeResponse getMyGrade(Long memberId) {
        int currentReviewCount = rankQueryService.findLatestByMemberIdAndRankType(MemberId.of(memberId), RankType.ALL).map(MemberReviewRank::getReviewCount).orElse(0);

        MemberGrade currentGrade = MemberGrade.fromReviewCount(currentReviewCount);
        MemberGrade nextGrade = currentGrade.isHigherThanOrEqual(MemberGrade.TEHA) ? null : MemberGrade.fromLevel(currentGrade.getLevel() + 1);

        int reviewsNeeded = 0;
        if (nextGrade != null) {
            reviewsNeeded = nextGrade.getMinReviewCount() - currentReviewCount;
        }

        return MyGradeResponse.from(
            currentGrade.name(),
            currentGrade.getDisplayName(),
            nextGrade != null ? nextGrade.name() : null,
            nextGrade != null ? nextGrade.getDisplayName() : null,
            currentReviewCount,
            reviewsNeeded
        );
    }
}
