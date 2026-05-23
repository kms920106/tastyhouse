package com.tastyhouse.core.domain.event.infrastructure.persistence;

import com.tastyhouse.core.domain.event.domain.model.EventWinner;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventWinnerJpaRepository extends JpaRepository<EventWinner, Long> {
}
