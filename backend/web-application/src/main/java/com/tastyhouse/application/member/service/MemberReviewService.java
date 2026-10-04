package com.tastyhouse.application.member.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.review.port.in.ReviewQueryUseCase;
import com.tastyhouse.application.review.port.out.MyReviewListItemResult;
import com.tastyhouse.application.shared.port.out.page.PageResult;

@Service
public class MemberReviewService {

    private final ReviewQueryUseCase reviewQueryUseCase;

    public MemberReviewService(ReviewQueryUseCase reviewQueryUseCase) {
        this.reviewQueryUseCase = reviewQueryUseCase;
    }

    @Transactional(readOnly = true)
    public PageResult<MyReviewListItemResult> getMyReviews(Long memberId, int page, int size) {
        return reviewQueryUseCase.findMyReviews(memberId, page, size);
    }

    @Transactional(readOnly = true)
    public long getMyReviewCount(Long memberId) {
        return reviewQueryUseCase.countVisibleReviewsByMemberId(memberId);
    }
}
