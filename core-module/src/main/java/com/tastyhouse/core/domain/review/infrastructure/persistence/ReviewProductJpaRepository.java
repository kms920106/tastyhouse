package com.tastyhouse.core.domain.review.infrastructure.persistence;

import com.tastyhouse.core.domain.review.domain.model.ReviewProduct;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewProductJpaRepository extends JpaRepository<ReviewProduct, Long> {
}
