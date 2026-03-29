package com.tastyhouse.core.repository.review;

import com.tastyhouse.core.entity.review.ReviewTag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewTagJpaRepository extends JpaRepository<ReviewTag, Long> {
}
