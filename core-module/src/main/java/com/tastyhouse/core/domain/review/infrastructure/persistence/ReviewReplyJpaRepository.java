package com.tastyhouse.core.domain.review.infrastructure.persistence;

import com.tastyhouse.core.domain.review.domain.model.ReviewReply;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewReplyJpaRepository extends JpaRepository<ReviewReply, Long> {
}
