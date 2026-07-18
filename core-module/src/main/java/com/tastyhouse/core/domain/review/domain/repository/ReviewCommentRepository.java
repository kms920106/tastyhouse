package com.tastyhouse.core.domain.review.domain.repository;

import java.util.List;
import java.util.Optional;

import com.tastyhouse.core.domain.review.domain.model.ReviewComment;
import com.tastyhouse.core.domain.review.domain.vo.ReviewCommentId;
import com.tastyhouse.core.domain.review.domain.vo.ReviewId;

public interface ReviewCommentRepository {

    List<ReviewComment> findByReviewIdOrderByCreatedAtDesc(ReviewId reviewId);

    Optional<ReviewComment> findById(ReviewCommentId commentId);

    ReviewComment save(ReviewComment comment);

    void deleteById(ReviewCommentId commentId);
}
