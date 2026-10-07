package com.tastyhouse.application.event.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.event.model.Event;
import com.tastyhouse.domain.event.vo.EventId;
import com.tastyhouse.application.event.port.in.EventDeleteCommand;
import com.tastyhouse.application.event.port.in.EventDeleteUseCase;
import com.tastyhouse.application.event.port.out.write.EventPersistencePort;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;

@Service
@Transactional
class EventDeleteService implements EventDeleteUseCase {

    private final EventPersistencePort eventPersistencePort;

    public EventDeleteService(EventPersistencePort eventPersistencePort) {
        this.eventPersistencePort = eventPersistencePort;
    }

    @Override
    public void deleteEvent(EventDeleteCommand command) {
        EventId eventId = EventId.of(command.eventId());
        Event event = findEventOrThrow(eventId);

        event.delete();
        eventPersistencePort.save(event);
    }

    private Event findEventOrThrow(EventId eventId) {
        return eventPersistencePort.findById(eventId)
            .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.EVENT_NOT_FOUND));
    }
}
