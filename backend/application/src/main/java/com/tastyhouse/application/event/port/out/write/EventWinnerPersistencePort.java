package com.tastyhouse.application.event.port.out.write;

import java.util.Optional;

import com.tastyhouse.domain.event.model.EventWinner;

public interface EventWinnerPersistencePort {
    Optional<EventWinner> findById(Long id);

    EventWinner save(EventWinner eventWinner);
}
