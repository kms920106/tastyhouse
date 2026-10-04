package com.tastyhouse.infrastructure.persistence.review.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

interface ReviewCommentJpaRepository extends JpaRepository<ReviewCommentJpaEntity, Long> {
}
