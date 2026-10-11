package com.tastyhouse.application.event.port.out.write;

import java.util.Optional;

import com.tastyhouse.domain.event.model.Event;
import com.tastyhouse.domain.event.vo.EventId;

public interface EventLoadPort {

    Optional<Event> findActiveById(EventId eventId);
}
