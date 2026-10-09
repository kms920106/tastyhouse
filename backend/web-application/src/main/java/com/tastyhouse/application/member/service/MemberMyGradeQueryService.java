package com.tastyhouse.application.member.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.member.model.MemberGrade;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.rank.model.RankType;
import com.tastyhouse.application.member.port.in.MemberMyGradeQueryUseCase;
import com.tastyhouse.application.member.port.out.MyGradeResult;
import com.tastyhouse.application.rank.port.out.RankQueryPort;

@Service
@Transactional(readOnly = true)
class MemberMyGradeQueryService implements MemberMyGradeQueryUseCase {

    private final RankQueryPort rankQueryPort;

    public MemberMyGradeQueryService(RankQueryPort rankQueryPort) {
        this.rankQueryPort = rankQueryPort;
    }

    @Override
    public MyGradeResult getMyGrade(Long memberId) {
        int currentReviewCount = rankQueryPort
            .findLatestReviewCount(MemberId.of(memberId).value(), RankType.ALL.name())
            .orElse(0);

        MemberGrade currentGrade = MemberGrade.fromReviewCount(currentReviewCount);
        MemberGrade nextGrade = currentGrade.isHigherThanOrEqual(MemberGrade.TEHA) ? null : MemberGrade.fromLevel(currentGrade.getLevel() + 1);

        int reviewsNeeded = 0;
        if (nextGrade != null) {
            reviewsNeeded = nextGrade.getMinReviewCount() - currentReviewCount;
        }

        return new MyGradeResult(
            currentGrade.name(),
            currentGrade.getDisplayName(),
            nextGrade != null ? nextGrade.name() : null,
            nextGrade != null ? nextGrade.getDisplayName() : null,
            currentReviewCount,
            reviewsNeeded
        );
    }
}
