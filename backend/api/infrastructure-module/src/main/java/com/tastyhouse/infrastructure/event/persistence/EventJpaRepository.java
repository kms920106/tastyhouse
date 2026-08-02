package com.tastyhouse.infrastructure.event.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EventJpaRepository extends JpaRepository<EventJpaEntity, Long> {
}
