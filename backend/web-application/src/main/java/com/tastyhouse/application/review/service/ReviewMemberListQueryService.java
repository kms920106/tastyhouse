package com.tastyhouse.application.review.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.review.port.in.ReviewMemberListQueryUseCase;
import com.tastyhouse.application.review.port.out.MyReviewListItemResult;
import com.tastyhouse.application.review.port.out.ReviewQueryPort;
import com.tastyhouse.application.shared.port.out.page.PageQuery;
import com.tastyhouse.application.shared.port.out.page.PageResult;

@Service
@Transactional(readOnly = true)
class ReviewMemberListQueryService implements ReviewMemberListQueryUseCase {

    private final ReviewQueryPort reviewQueryPort;

    public ReviewMemberListQueryService(ReviewQueryPort reviewQueryPort) {
        this.reviewQueryPort = reviewQueryPort;
    }

    @Override
    public PageResult<MyReviewListItemResult> findMemberReviews(Long memberId, int page, int size) {
        return reviewQueryPort.findReviewsByMemberId(memberId, PageQuery.of(page, size));
    }
}
