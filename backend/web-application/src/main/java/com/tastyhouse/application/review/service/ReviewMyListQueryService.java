package com.tastyhouse.application.review.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.review.port.in.ReviewMyListQueryUseCase;
import com.tastyhouse.application.review.port.out.MyReviewListItemResult;
import com.tastyhouse.application.review.port.out.ReviewQueryPort;
import com.tastyhouse.application.shared.port.out.page.PageQuery;
import com.tastyhouse.application.shared.port.out.page.PageResult;

@Service
@Transactional(readOnly = true)
class ReviewMyListQueryService implements ReviewMyListQueryUseCase {

    private final ReviewQueryPort reviewQueryPort;

    public ReviewMyListQueryService(ReviewQueryPort reviewQueryPort) {
        this.reviewQueryPort = reviewQueryPort;
    }

    @Override
    public PageResult<MyReviewListItemResult> findMyReviews(Long memberId, int page, int size) {
        return reviewQueryPort.findMyReviews(memberId, PageQuery.of(page, size));
    }
}
