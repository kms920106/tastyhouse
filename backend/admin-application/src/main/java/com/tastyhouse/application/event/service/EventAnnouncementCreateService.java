package com.tastyhouse.application.event.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.event.model.Event;
import com.tastyhouse.domain.event.model.EventAnnouncement;
import com.tastyhouse.domain.event.vo.EventId;
import com.tastyhouse.application.event.port.in.EventAnnouncementCreateCommand;
import com.tastyhouse.application.event.port.in.EventAnnouncementCreateUseCase;
import com.tastyhouse.application.event.port.out.write.EventAnnouncementPersistencePort;
import com.tastyhouse.application.event.port.out.write.EventPersistencePort;
import com.tastyhouse.application.shared.exception.AdminErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;

@Service
@Transactional
class EventAnnouncementCreateService implements EventAnnouncementCreateUseCase {

    private final EventPersistencePort eventPersistencePort;
    private final EventAnnouncementPersistencePort eventAnnouncementPersistencePort;

    public EventAnnouncementCreateService(
        EventPersistencePort eventPersistencePort,
        EventAnnouncementPersistencePort eventAnnouncementPersistencePort
    ) {
        this.eventPersistencePort = eventPersistencePort;
        this.eventAnnouncementPersistencePort = eventAnnouncementPersistencePort;
    }

    @Override
    public Long createAnnouncement(EventAnnouncementCreateCommand command) {
        EventId eventId = EventId.of(command.eventId());
        findEventOrThrow(eventId);

        if (eventAnnouncementPersistencePort.existsByEventId(eventId)) {
            throw new ApplicationException(AdminErrorCode.EVENT_ANNOUNCEMENT_ALREADY_EXISTS);
        }

        EventAnnouncement announcement = EventAnnouncement.of(eventId, command.name(), command.content(), command.announcedAt());
        EventAnnouncement saved = eventAnnouncementPersistencePort.save(announcement);
        return saved.getId();
    }

    private Event findEventOrThrow(EventId eventId) {
        return eventPersistencePort.findById(eventId)
            .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.EVENT_NOT_FOUND));
    }
}
