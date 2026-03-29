package com.tastyhouse.core.repository.review;

import com.tastyhouse.core.entity.review.ReviewComment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewCommentJpaRepository extends JpaRepository<ReviewComment, Long> {
}
