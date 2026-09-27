package com.tastyhouse.application.review.port.out.write;

public interface ReviewLikeStatePort {

    boolean existsByReviewIdAndMemberId(Long reviewId, Long memberId);

    void deleteByReviewIdAndMemberId(Long reviewId, Long memberId);

    ReviewLikeState save(ReviewLikeState state);
}
