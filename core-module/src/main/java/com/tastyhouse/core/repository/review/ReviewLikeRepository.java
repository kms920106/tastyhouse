package com.tastyhouse.core.repository.review;

import com.tastyhouse.core.entity.review.ReviewLike;

import java.util.List;
import java.util.Map;

public interface ReviewLikeRepository {

    Map<Long, Long> countByReviewIdIn(List<Long> reviewIds);

    boolean existsByReviewIdAndMemberId(Long reviewId, Long memberId);

    void deleteByReviewIdAndMemberId(Long reviewId, Long memberId);

    ReviewLike save(ReviewLike reviewLike);
}
