package com.tastyhouse.infrastructure.persistence.review.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewLikeJpaRepository extends JpaRepository<ReviewLikeJpaEntity, Long> {
}
