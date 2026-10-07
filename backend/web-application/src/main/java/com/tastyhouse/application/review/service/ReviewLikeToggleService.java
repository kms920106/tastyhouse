package com.tastyhouse.application.review.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.review.vo.ReviewId;
import com.tastyhouse.application.review.port.in.ReviewLikeToggleCommand;
import com.tastyhouse.application.review.port.in.ReviewLikeToggleUseCase;

@Service
@Transactional
class ReviewLikeToggleService implements ReviewLikeToggleUseCase {

    private final ReviewLifecycleService reviewLifecycleService;

    public ReviewLikeToggleService(ReviewLifecycleService reviewLifecycleService) {
        this.reviewLifecycleService = reviewLifecycleService;
    }

    @Override
    public boolean toggleReviewLike(ReviewLikeToggleCommand command) {
        ReviewId targetReviewId = ReviewId.of(command.reviewId());
        return reviewLifecycleService.toggleLike(targetReviewId, MemberId.of(command.memberId()));
    }
}
