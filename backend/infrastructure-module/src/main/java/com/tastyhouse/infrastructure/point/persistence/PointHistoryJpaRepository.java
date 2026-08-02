package com.tastyhouse.infrastructure.point.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PointHistoryJpaRepository extends JpaRepository<PointHistoryJpaEntity, Long> {
}
