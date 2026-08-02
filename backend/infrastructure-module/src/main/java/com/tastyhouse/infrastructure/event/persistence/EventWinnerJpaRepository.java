package com.tastyhouse.infrastructure.event.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EventWinnerJpaRepository extends JpaRepository<EventWinnerJpaEntity, Long> {
}
