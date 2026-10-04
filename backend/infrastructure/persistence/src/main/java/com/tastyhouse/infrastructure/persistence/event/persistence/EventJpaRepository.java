package com.tastyhouse.infrastructure.persistence.event.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

interface EventJpaRepository extends JpaRepository<EventJpaEntity, Long> {
}
