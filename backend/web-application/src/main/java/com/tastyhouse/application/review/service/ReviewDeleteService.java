package com.tastyhouse.application.review.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.review.model.Review;
import com.tastyhouse.domain.review.vo.ReviewId;
import com.tastyhouse.application.review.port.in.ReviewDeleteCommand;
import com.tastyhouse.application.review.port.in.ReviewDeleteUseCase;
import com.tastyhouse.application.review.port.out.write.ReviewPersistencePort;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;

@Service
@Transactional
class ReviewDeleteService implements ReviewDeleteUseCase {

    private final ReviewLifecycleService reviewLifecycleService;
    private final ReviewPersistencePort reviewPersistencePort;

    public ReviewDeleteService(
        ReviewLifecycleService reviewLifecycleService,
        ReviewPersistencePort reviewPersistencePort
    ) {
        this.reviewLifecycleService = reviewLifecycleService;
        this.reviewPersistencePort = reviewPersistencePort;
    }

    @Override
    public void deleteReview(ReviewDeleteCommand command) {
        ReviewId targetReviewId = ReviewId.of(command.reviewId());
        Review review = reviewPersistencePort.findById(targetReviewId)
            .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.REVIEW_NOT_FOUND));

        reviewLifecycleService.removeOwnedBy(targetReviewId, MemberId.of(command.memberId()), review.getProductId());
    }
}
