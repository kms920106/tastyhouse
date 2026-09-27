package com.tastyhouse.application.event.store;

import java.util.Optional;

import com.tastyhouse.application.event.port.out.write.EventWinnerStatePort;
import com.tastyhouse.domain.event.model.EventWinner;

public class EventWinnerStore implements EventWinnerRepository {
    private final EventWinnerStatePort eventWinnerStatePort;

    public EventWinnerStore(EventWinnerStatePort eventWinnerStatePort) {
        this.eventWinnerStatePort = eventWinnerStatePort;
    }

    @Override
    public Optional<EventWinner> findById(Long id) {
        return eventWinnerStatePort.findById(id).map(EventWinnerStateMapper::toDomain);
    }

    @Override
    public EventWinner save(EventWinner eventWinner) {
        return EventWinnerStateMapper.toDomain(eventWinnerStatePort.save(EventWinnerStateMapper.toState(eventWinner)));
    }
}
