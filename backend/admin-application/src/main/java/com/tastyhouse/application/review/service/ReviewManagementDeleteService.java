package com.tastyhouse.application.review.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.review.vo.ReviewId;
import com.tastyhouse.application.review.port.in.ReviewManagementDeleteCommand;
import com.tastyhouse.application.review.port.in.ReviewManagementDeleteUseCase;

@Service
@Transactional
class ReviewManagementDeleteService implements ReviewManagementDeleteUseCase {

    private final ReviewLifecycleService reviewLifecycleService;

    public ReviewManagementDeleteService(ReviewLifecycleService reviewLifecycleService) {
        this.reviewLifecycleService = reviewLifecycleService;
    }

    @Override
    public void deleteReview(ReviewManagementDeleteCommand command) {
        Long id = command.reviewId();
        ReviewId reviewId = ReviewId.of(id);
        reviewLifecycleService.remove(reviewId);
    }
}
