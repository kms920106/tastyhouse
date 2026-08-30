package com.tastyhouse.infrastructure.review.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewCommentJpaRepository extends JpaRepository<ReviewCommentJpaEntity, Long> {
}
