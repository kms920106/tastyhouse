package com.tastyhouse.core.domain.review.domain.repository;

import com.tastyhouse.core.domain.review.domain.model.ReviewLike;

import java.util.List;
import java.util.Map;

public interface ReviewLikeRepository {

    Map<Long, Long> countByReviewIdIn(List<Long> reviewIds);

    boolean existsByReviewIdAndMemberId(Long reviewId, Long memberId);

    void deleteByReviewIdAndMemberId(Long reviewId, Long memberId);

    ReviewLike save(ReviewLike reviewLike);
}
