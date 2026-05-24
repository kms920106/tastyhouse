package com.tastyhouse.core.domain.review.infrastructure.persistence;

import com.tastyhouse.core.domain.review.domain.model.ReviewLike;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewLikeJpaRepository extends JpaRepository<ReviewLike, Long> {
}
