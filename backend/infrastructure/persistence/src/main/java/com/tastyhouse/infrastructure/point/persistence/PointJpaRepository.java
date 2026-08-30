package com.tastyhouse.infrastructure.point.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PointJpaRepository extends JpaRepository<PointJpaEntity, Long> {
}
