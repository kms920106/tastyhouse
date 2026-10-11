package com.tastyhouse.application.event.port.out.write;

import com.tastyhouse.domain.event.model.Event;

public interface EventSavePort {

    Event save(Event event);
}
