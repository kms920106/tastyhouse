package com.tastyhouse.core.domain.review.domain.repository;

import java.util.List;

import com.tastyhouse.core.domain.review.domain.model.ReviewComment;
import com.tastyhouse.core.domain.review.domain.vo.ReviewId;

public interface ReviewCommentRepository {

    List<ReviewComment> findByReviewIdOrderByCreatedAtDesc(ReviewId reviewId);

    ReviewComment save(ReviewComment comment);
}
