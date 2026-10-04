package com.tastyhouse.infrastructure.persistence.review.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

interface ReviewLikeJpaRepository extends JpaRepository<ReviewLikeJpaEntity, Long> {
}
