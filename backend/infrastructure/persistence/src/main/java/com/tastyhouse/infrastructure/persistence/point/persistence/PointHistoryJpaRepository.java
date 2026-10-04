package com.tastyhouse.infrastructure.persistence.point.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

interface PointHistoryJpaRepository extends JpaRepository<PointHistoryJpaEntity, Long> {
}
