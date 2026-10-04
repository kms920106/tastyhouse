package com.tastyhouse.infrastructure.persistence.event.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

interface EventWinnerJpaRepository extends JpaRepository<EventWinnerJpaEntity, Long> {
}
