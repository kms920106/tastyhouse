package com.tastyhouse.application.event.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.event.model.Event;
import com.tastyhouse.domain.event.vo.EventId;
import com.tastyhouse.application.event.port.in.EventDeleteCommand;
import com.tastyhouse.application.event.port.in.EventDeleteUseCase;
import com.tastyhouse.application.event.port.out.write.EventLoadPort;
import com.tastyhouse.application.event.port.out.write.EventSavePort;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;

@Service
@Transactional
class EventDeleteService implements EventDeleteUseCase {

    private final EventLoadPort eventLoadPort;
    private final EventSavePort eventSavePort;

    public EventDeleteService(EventLoadPort eventLoadPort, EventSavePort eventSavePort) {
        this.eventLoadPort = eventLoadPort;
        this.eventSavePort = eventSavePort;
    }

    @Override
    public void deleteEvent(EventDeleteCommand command) {
        EventId eventId = EventId.of(command.eventId());
        Event event = findEventOrThrow(eventId);

        event.delete();
        eventSavePort.save(event);
    }

    private Event findEventOrThrow(EventId eventId) {
        return eventLoadPort.findById(eventId)
            .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.EVENT_NOT_FOUND));
    }
}
