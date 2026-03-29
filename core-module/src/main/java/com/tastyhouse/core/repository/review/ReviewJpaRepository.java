package com.tastyhouse.core.repository.review;

import com.tastyhouse.core.entity.review.Review;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewJpaRepository extends JpaRepository<Review, Long> {
}