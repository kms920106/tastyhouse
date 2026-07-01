package com.tastyhouse.core.domain.review.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tastyhouse.core.domain.review.domain.model.Review;

public interface ReviewJpaRepository extends JpaRepository<Review, Long> {
}
