package com.tastyhouse.infrastructure.persistence.review.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewJpaRepository extends JpaRepository<ReviewJpaEntity, Long> {
}
