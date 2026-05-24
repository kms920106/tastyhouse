package com.tastyhouse.core.domain.review.infrastructure.persistence;

import com.tastyhouse.core.domain.review.domain.model.ReviewComment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewCommentJpaRepository extends JpaRepository<ReviewComment, Long> {
}
