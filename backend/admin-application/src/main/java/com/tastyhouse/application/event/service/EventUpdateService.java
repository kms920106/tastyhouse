package com.tastyhouse.application.event.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.event.model.Event;
import com.tastyhouse.domain.event.model.EventStatus;
import com.tastyhouse.domain.event.vo.EventId;
import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.application.event.port.in.EventUpdateCommand;
import com.tastyhouse.application.event.port.in.EventUpdateUseCase;
import com.tastyhouse.application.event.port.out.write.EventLoadPort;
import com.tastyhouse.application.event.port.out.write.EventSavePort;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;

@Service
@Transactional
class EventUpdateService implements EventUpdateUseCase {

    private final EventLoadPort eventLoadPort;
    private final EventSavePort eventSavePort;

    public EventUpdateService(EventLoadPort eventLoadPort, EventSavePort eventSavePort) {
        this.eventLoadPort = eventLoadPort;
        this.eventSavePort = eventSavePort;
    }

    @Override
    public void updateEvent(EventUpdateCommand command) {
        Long thumbnailImageFileId = command.thumbnailImageFileId();
        Long bannerImageFileId = command.bannerImageFileId();
        EventId eventId = EventId.of(command.eventId());
        Event event = findEventOrThrow(eventId);

        event.update(
            command.name(),
            command.description(),
            command.subtitle(),
            thumbnailImageFileId == null ? null : UploadedFileId.of(thumbnailImageFileId),
            bannerImageFileId == null ? null : UploadedFileId.of(bannerImageFileId),
            command.contentHtml(),
            EventStatus.from(command.status()),
            command.startAt(),
            command.endAt()
        );
        eventSavePort.save(event);
    }

    private Event findEventOrThrow(EventId eventId) {
        return eventLoadPort.findById(eventId)
            .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.EVENT_NOT_FOUND));
    }
}
