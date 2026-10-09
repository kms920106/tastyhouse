package com.tastyhouse.infrastructure.jpa.event.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

interface EventJpaRepository extends JpaRepository<EventJpaEntity, Long> {
}
