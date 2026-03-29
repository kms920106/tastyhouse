package com.tastyhouse.core.repository.review;

import com.tastyhouse.core.entity.review.ReviewComment;

import java.util.List;
import java.util.Map;

public interface ReviewCommentRepository {

    List<ReviewComment> findByReviewIdOrderByCreatedAtDesc(Long reviewId);

    Map<Long, Long> countByReviewIdIn(List<Long> reviewIds);

    ReviewComment save(ReviewComment comment);
}
