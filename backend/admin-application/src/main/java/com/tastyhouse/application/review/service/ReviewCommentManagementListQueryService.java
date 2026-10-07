package com.tastyhouse.application.review.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.review.vo.ReviewId;
import com.tastyhouse.application.review.port.in.ReviewCommentManagementListQueryUseCase;
import com.tastyhouse.application.review.port.out.ReviewCommentListItemResult;
import com.tastyhouse.application.review.port.out.ReviewManagementQueryPort;

@Service
@Transactional(readOnly = true)
class ReviewCommentManagementListQueryService implements ReviewCommentManagementListQueryUseCase {

    private final ReviewManagementQueryPort reviewManagementQueryPort;

    public ReviewCommentManagementListQueryService(ReviewManagementQueryPort reviewManagementQueryPort) {
        this.reviewManagementQueryPort = reviewManagementQueryPort;
    }

    @Override
    public List<ReviewCommentListItemResult> getComments(Long id) {
        ReviewId reviewId = ReviewId.of(id);
        return reviewManagementQueryPort.findCommentsIncludingHidden(reviewId.value());
    }
}
