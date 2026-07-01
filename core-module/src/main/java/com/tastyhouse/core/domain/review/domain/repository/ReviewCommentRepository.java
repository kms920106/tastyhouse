package com.tastyhouse.core.domain.review.domain.repository;

import java.util.List;

import com.tastyhouse.core.domain.review.domain.model.ReviewComment;

public interface ReviewCommentRepository {

    List<ReviewComment> findByReviewIdOrderByCreatedAtDesc(Long reviewId);

    ReviewComment save(ReviewComment comment);
}
