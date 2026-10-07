package com.tastyhouse.application.review.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.review.port.in.ReviewManagementListQueryUseCase;
import com.tastyhouse.application.review.port.out.ReviewListItemResult;
import com.tastyhouse.application.review.port.out.ReviewManagementQueryPort;
import com.tastyhouse.application.review.port.out.ReviewSearchCondition;
import com.tastyhouse.application.shared.port.out.page.PageQuery;
import com.tastyhouse.application.shared.port.out.page.PageResult;

@Service
@Transactional(readOnly = true)
class ReviewManagementListQueryService implements ReviewManagementListQueryUseCase {

    private final ReviewManagementQueryPort reviewManagementQueryPort;

    public ReviewManagementListQueryService(ReviewManagementQueryPort reviewManagementQueryPort) {
        this.reviewManagementQueryPort = reviewManagementQueryPort;
    }

    @Override
    public PageResult<ReviewListItemResult> getReviews(
        Long shopId,
        Long productId,
        Long memberId,
        Boolean hidden,
        Boolean ownerOnly,
        String content,
        Double minRating,
        Double maxRating,
        int page,
        int size
    ) {
        ReviewSearchCondition condition = ReviewSearchCondition.of(shopId, productId, memberId, hidden, ownerOnly, content, minRating, maxRating);
        return reviewManagementQueryPort.findReviews(condition, PageQuery.of(page, size));
    }
}
