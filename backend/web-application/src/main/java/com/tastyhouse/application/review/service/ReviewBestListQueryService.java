package com.tastyhouse.application.review.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.review.port.in.ReviewBestListQueryUseCase;
import com.tastyhouse.application.review.port.out.BestReviewListItemResult;
import com.tastyhouse.application.review.port.out.ReviewFeedQueryPort;
import com.tastyhouse.application.shared.port.out.page.PageQuery;
import com.tastyhouse.application.shared.port.out.page.PageResult;

@Service
@Transactional(readOnly = true)
class ReviewBestListQueryService implements ReviewBestListQueryUseCase {

    private final ReviewFeedQueryPort reviewFeedQueryPort;

    public ReviewBestListQueryService(ReviewFeedQueryPort reviewFeedQueryPort) {
        this.reviewFeedQueryPort = reviewFeedQueryPort;
    }

    @Override
    public PageResult<BestReviewListItemResult> searchBestReviewList(int page, int size) {
        return reviewFeedQueryPort.findBestReviews(PageQuery.of(page, size));
    }
}
