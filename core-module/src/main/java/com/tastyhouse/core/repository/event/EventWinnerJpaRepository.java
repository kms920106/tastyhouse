package com.tastyhouse.core.repository.event;

import com.tastyhouse.core.entity.event.EventWinner;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventWinnerJpaRepository extends JpaRepository<EventWinner, Long> {
}
