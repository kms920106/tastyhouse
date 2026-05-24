package com.tastyhouse.core.domain.review.domain.repository;

import com.tastyhouse.core.domain.review.domain.model.ReviewComment;

import java.util.List;
import java.util.Map;

public interface ReviewCommentRepository {

    List<ReviewComment> findByReviewIdOrderByCreatedAtDesc(Long reviewId);

    Map<Long, Long> countByReviewIdIn(List<Long> reviewIds);

    ReviewComment save(ReviewComment comment);
}
