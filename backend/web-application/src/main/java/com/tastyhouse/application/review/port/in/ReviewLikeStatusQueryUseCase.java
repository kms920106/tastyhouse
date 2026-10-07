package com.tastyhouse.application.review.port.in;

public interface ReviewLikeStatusQueryUseCase {

    boolean isLiked(Long reviewId, Long memberId);
}
