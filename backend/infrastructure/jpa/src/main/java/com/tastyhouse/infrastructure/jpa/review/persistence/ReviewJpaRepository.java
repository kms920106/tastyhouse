package com.tastyhouse.infrastructure.jpa.review.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

interface ReviewJpaRepository extends JpaRepository<ReviewJpaEntity, Long> {
}
