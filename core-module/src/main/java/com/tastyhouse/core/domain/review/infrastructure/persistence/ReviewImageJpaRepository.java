package com.tastyhouse.core.domain.review.infrastructure.persistence;

import com.tastyhouse.core.domain.review.domain.model.ReviewImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewImageJpaRepository extends JpaRepository<ReviewImage, Long> {
}
