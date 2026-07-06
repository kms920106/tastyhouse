package com.tastyhouse.core.domain.review.domain.repository;

import com.tastyhouse.core.domain.review.domain.model.ReviewLike;
import com.tastyhouse.core.domain.review.domain.vo.ReviewId;

public interface ReviewLikeRepository {

    boolean existsByReviewIdAndMemberId(ReviewId reviewId, Long memberId);

    void deleteByReviewIdAndMemberId(ReviewId reviewId, Long memberId);

    ReviewLike save(ReviewLike reviewLike);
}
