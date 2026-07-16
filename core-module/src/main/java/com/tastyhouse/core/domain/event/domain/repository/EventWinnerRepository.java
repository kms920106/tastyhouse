package com.tastyhouse.core.domain.event.domain.repository;

import java.util.List;
import java.util.Optional;

import com.tastyhouse.core.domain.event.domain.model.EventWinner;
import com.tastyhouse.core.domain.event.domain.vo.EventId;

public interface EventWinnerRepository {

    List<EventWinner> findByEventIdOrderByRankNo(EventId eventId);

    Optional<EventWinner> findById(Long id);

    EventWinner save(EventWinner eventWinner);
}
