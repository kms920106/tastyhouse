package com.tastyhouse.application.review.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.review.vo.ReviewId;
import com.tastyhouse.application.review.port.in.ReviewManagementDetailQueryUseCase;
import com.tastyhouse.application.review.port.out.ReviewManagementDetailResult;
import com.tastyhouse.application.review.port.out.ReviewManagementQueryPort;
import com.tastyhouse.application.review.port.out.ReviewTagQueryPort;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;

@Service
@Transactional(readOnly = true)
class ReviewManagementDetailQueryService implements ReviewManagementDetailQueryUseCase {

    private final ReviewManagementQueryPort reviewManagementQueryPort;
    private final ReviewTagQueryPort reviewTagQueryPort;

    public ReviewManagementDetailQueryService(ReviewManagementQueryPort reviewManagementQueryPort, ReviewTagQueryPort reviewTagQueryPort) {
        this.reviewManagementQueryPort = reviewManagementQueryPort;
        this.reviewTagQueryPort = reviewTagQueryPort;
    }

    @Override
    public ReviewManagementDetailResult getReview(Long id) {
        ReviewId reviewId = ReviewId.of(id);
        ReviewManagementDetailResult detail = reviewManagementQueryPort.findReviewManagementDetail(reviewId.value())
            .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.REVIEW_NOT_FOUND));

        List<Long> tagIds = reviewTagQueryPort.findTagIdsByReviewId(reviewId.value());
        if (!tagIds.isEmpty()) {
            detail = detail.withTagNames(reviewTagQueryPort.findTagNamesByIds(tagIds));
        }

        return detail;
    }
}
