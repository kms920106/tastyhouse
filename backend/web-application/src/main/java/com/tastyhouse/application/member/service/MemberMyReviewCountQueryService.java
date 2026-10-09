package com.tastyhouse.application.member.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.member.port.in.MemberMyReviewCountQueryUseCase;
import com.tastyhouse.application.review.port.in.ReviewMemberCountQueryUseCase;

@Service
@Transactional(readOnly = true)
class MemberMyReviewCountQueryService implements MemberMyReviewCountQueryUseCase {

    private final ReviewMemberCountQueryUseCase reviewMemberCountQueryUseCase;

    public MemberMyReviewCountQueryService(ReviewMemberCountQueryUseCase reviewMemberCountQueryUseCase) {
        this.reviewMemberCountQueryUseCase = reviewMemberCountQueryUseCase;
    }

    @Override
    public long getMyReviewCount(Long memberId) {
        return reviewMemberCountQueryUseCase.countVisibleReviewsByMemberId(memberId);
    }
}
