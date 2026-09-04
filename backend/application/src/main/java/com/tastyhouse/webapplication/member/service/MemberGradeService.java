package com.tastyhouse.webapplication.member.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.member.model.MemberGrade;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.rank.model.MemberReviewRank;
import com.tastyhouse.domain.rank.model.RankType;
import com.tastyhouse.domain.rank.repository.MemberReviewRankRepository;
import com.tastyhouse.webapplication.member.port.out.MyGradeResult;

@Service
public class MemberGradeService {

    private final MemberReviewRankRepository memberReviewRankRepository;

    public MemberGradeService(MemberReviewRankRepository memberReviewRankRepository) {
        this.memberReviewRankRepository = memberReviewRankRepository;
    }

    @Transactional(readOnly = true)
    public MyGradeResult getMyGrade(Long memberId) {
        int currentReviewCount = memberReviewRankRepository.findLatestByMemberIdAndRankType(MemberId.of(memberId), RankType.ALL)
            .map(MemberReviewRank::getReviewCount)
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
