package com.tastyhouse.core.repository.review;

import com.tastyhouse.core.entity.review.ReviewLike;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewLikeJpaRepository extends JpaRepository<ReviewLike, Long> {
}
