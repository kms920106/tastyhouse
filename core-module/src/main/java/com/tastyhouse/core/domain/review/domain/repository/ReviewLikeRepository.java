package com.tastyhouse.core.domain.review.domain.repository;

import com.tastyhouse.core.domain.review.domain.model.ReviewLike;

public interface ReviewLikeRepository {

    boolean existsByReviewIdAndMemberId(Long reviewId, Long memberId);

    void deleteByReviewIdAndMemberId(Long reviewId, Long memberId);

    ReviewLike save(ReviewLike reviewLike);
}
