package com.tastyhouse.application.member.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.review.port.in.ReviewMemberCountQueryUseCase;
import com.tastyhouse.application.review.port.in.ReviewMyListQueryUseCase;
import com.tastyhouse.application.review.port.out.MyReviewListItemResult;
import com.tastyhouse.application.shared.port.out.page.PageResult;

@Service
public class MemberReviewService {

    private final ReviewMyListQueryUseCase reviewMyListQueryUseCase;
    private final ReviewMemberCountQueryUseCase reviewMemberCountQueryUseCase;

    public MemberReviewService(
        ReviewMyListQueryUseCase reviewMyListQueryUseCase,
        ReviewMemberCountQueryUseCase reviewMemberCountQueryUseCase
    ) {
        this.reviewMyListQueryUseCase = reviewMyListQueryUseCase;
        this.reviewMemberCountQueryUseCase = reviewMemberCountQueryUseCase;
    }

    @Transactional(readOnly = true)
    public PageResult<MyReviewListItemResult> getMyReviews(Long memberId, int page, int size) {
        return reviewMyListQueryUseCase.findMyReviews(memberId, page, size);
    }

    @Transactional(readOnly = true)
    public long getMyReviewCount(Long memberId) {
        return reviewMemberCountQueryUseCase.countVisibleReviewsByMemberId(memberId);
    }
}
