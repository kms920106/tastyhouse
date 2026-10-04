package com.tastyhouse.infrastructure.persistence.review.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

interface ReviewTagJpaRepository extends JpaRepository<ReviewTagJpaEntity, Long> {
}
