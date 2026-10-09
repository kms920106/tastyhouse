package com.tastyhouse.application.member.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.member.port.in.MemberMyReviewListQueryUseCase;
import com.tastyhouse.application.review.port.in.ReviewMyListQueryUseCase;
import com.tastyhouse.application.review.port.out.MyReviewListItemResult;
import com.tastyhouse.application.shared.port.out.page.PageResult;

@Service
@Transactional(readOnly = true)
class MemberMyReviewListQueryService implements MemberMyReviewListQueryUseCase {

    private final ReviewMyListQueryUseCase reviewMyListQueryUseCase;

    public MemberMyReviewListQueryService(ReviewMyListQueryUseCase reviewMyListQueryUseCase) {
        this.reviewMyListQueryUseCase = reviewMyListQueryUseCase;
    }

    @Override
    public PageResult<MyReviewListItemResult> getMyReviews(Long memberId, int page, int size) {
        return reviewMyListQueryUseCase.findMyReviews(memberId, page, size);
    }
}
