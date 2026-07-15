package com.tastyhouse.core.domain.event.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tastyhouse.core.domain.event.domain.model.Event;

public interface EventJpaRepository extends JpaRepository<Event, Long> {
}
