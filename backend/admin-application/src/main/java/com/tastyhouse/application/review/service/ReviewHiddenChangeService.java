package com.tastyhouse.application.review.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.review.model.Review;
import com.tastyhouse.domain.review.vo.ReviewId;
import com.tastyhouse.application.review.port.in.ReviewHiddenChangeCommand;
import com.tastyhouse.application.review.port.in.ReviewHiddenChangeUseCase;
import com.tastyhouse.application.review.port.out.write.ReviewPersistencePort;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;

@Service
@Transactional
class ReviewHiddenChangeService implements ReviewHiddenChangeUseCase {

    private final ReviewPersistencePort reviewPersistencePort;

    public ReviewHiddenChangeService(ReviewPersistencePort reviewPersistencePort) {
        this.reviewPersistencePort = reviewPersistencePort;
    }

    @Override
    public void changeReviewHidden(ReviewHiddenChangeCommand command) {
        Long id = command.reviewId();
        boolean hidden = command.hidden();
        ReviewId reviewId = ReviewId.of(id);
        Review review = reviewPersistencePort.findById(reviewId)
            .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.REVIEW_NOT_FOUND));

        if (hidden) {
            review.hide();
        } else {
            review.unhide();
        }

        reviewPersistencePort.save(review);
    }
}
