package com.tastyhouse.application.event.port.out.write;

import java.util.Optional;

public interface EventWinnerStatePort {
    Optional<EventWinnerState> findById(Long id);

    EventWinnerState save(EventWinnerState state);
}
