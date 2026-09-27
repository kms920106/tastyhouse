package com.tastyhouse.application.event.store;

import java.util.Optional;

import com.tastyhouse.domain.event.model.Event;
import com.tastyhouse.domain.event.vo.EventId;

public interface EventRepository {
    Optional<Event> findById(EventId eventId);

    Event save(Event event);
}
