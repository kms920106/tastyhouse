package com.tastyhouse.domain.event.repository;

import java.util.Optional;

import com.tastyhouse.domain.event.model.EventWinner;

public interface EventWinnerRepository {
    Optional<EventWinner> findById(Long id);

    EventWinner save(EventWinner eventWinner);
}
