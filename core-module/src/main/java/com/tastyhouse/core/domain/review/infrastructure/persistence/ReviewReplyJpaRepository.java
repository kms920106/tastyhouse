package com.tastyhouse.core.domain.review.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tastyhouse.core.domain.review.domain.model.ReviewReply;

public interface ReviewReplyJpaRepository extends JpaRepository<ReviewReply, Long> {
}
