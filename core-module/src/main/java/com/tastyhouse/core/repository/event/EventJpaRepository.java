package com.tastyhouse.core.repository.event;

import com.tastyhouse.core.entity.event.Event;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventJpaRepository extends JpaRepository<Event, Long> {
}
