package com.tastyhouse.application.event.store;

import java.util.Optional;

import com.tastyhouse.domain.event.model.Event;
import com.tastyhouse.domain.event.vo.EventId;
import com.tastyhouse.application.event.port.out.write.EventStatePort;

public class EventStore implements EventRepository {
    private final EventStatePort eventStatePort;

    public EventStore(EventStatePort eventStatePort) {
        this.eventStatePort = eventStatePort;
    }

    @Override
    public Optional<Event> findById(EventId eventId) {
        return eventStatePort.findById(eventId.value()).map(EventStateMapper::toDomain);
    }

    @Override
    public Event save(Event event) {
        return EventStateMapper.toDomain(eventStatePort.save(EventStateMapper.toState(event)));
    }
}
