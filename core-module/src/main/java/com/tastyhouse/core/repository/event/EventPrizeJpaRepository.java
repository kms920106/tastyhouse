package com.tastyhouse.core.repository.event;

import com.tastyhouse.core.entity.event.EventPrize;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventPrizeJpaRepository extends JpaRepository<EventPrize, Long> {
}
