package com.tastyhouse.core.domain.review.infrastructure.persistence;

import com.tastyhouse.core.domain.review.domain.model.ReviewTag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewTagJpaRepository extends JpaRepository<ReviewTag, Long> {
}
