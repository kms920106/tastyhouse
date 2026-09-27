package com.tastyhouse.application.event.port.out.write;

import java.util.Optional;

public interface EventStatePort {
    Optional<EventState> findById(Long eventId);

    EventState save(EventState state);
}
