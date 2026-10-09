package com.tastyhouse.application.event.port.out.write;

import java.util.Optional;

import com.tastyhouse.domain.event.model.EventWinner;

public interface EventWinnerLoadPort {

    Optional<EventWinner> findActiveById(Long id);
}
