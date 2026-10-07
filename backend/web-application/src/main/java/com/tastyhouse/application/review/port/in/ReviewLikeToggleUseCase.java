package com.tastyhouse.application.review.port.in;

public interface ReviewLikeToggleUseCase {

    boolean toggleReviewLike(ReviewLikeToggleCommand command);
}
