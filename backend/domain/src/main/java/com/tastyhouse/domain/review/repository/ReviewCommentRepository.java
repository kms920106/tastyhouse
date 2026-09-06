package com.tastyhouse.domain.review.repository;

import java.util.Optional;

import com.tastyhouse.domain.review.model.ReviewComment;
import com.tastyhouse.domain.review.vo.ReviewCommentId;

public interface ReviewCommentRepository {
    Optional<ReviewComment> findById(ReviewCommentId commentId);

    ReviewComment save(ReviewComment comment);

    void deleteById(ReviewCommentId commentId);
}
