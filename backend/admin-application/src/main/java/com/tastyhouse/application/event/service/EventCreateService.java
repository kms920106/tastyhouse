package com.tastyhouse.application.event.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.event.model.Event;
import com.tastyhouse.domain.event.model.EventStatus;
import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.application.event.port.in.EventCreateCommand;
import com.tastyhouse.application.event.port.in.EventCreateUseCase;
import com.tastyhouse.application.event.port.out.write.EventSavePort;

@Service
@Transactional
class EventCreateService implements EventCreateUseCase {

    private final EventSavePort eventSavePort;

    public EventCreateService(EventSavePort eventSavePort) {
        this.eventSavePort = eventSavePort;
    }

    @Override
    public Long createEvent(EventCreateCommand command) {
        Long thumbnailImageFileId = command.thumbnailImageFileId();
        Long bannerImageFileId = command.bannerImageFileId();

        Event event = Event.of(
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
        Event saved = eventSavePort.save(event);
        return saved.getEventId().value();
    }
}
