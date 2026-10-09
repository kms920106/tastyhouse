package com.tastyhouse.infrastructure.jpa.point.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

interface PointHistoryJpaRepository extends JpaRepository<PointHistoryJpaEntity, Long> {
}
