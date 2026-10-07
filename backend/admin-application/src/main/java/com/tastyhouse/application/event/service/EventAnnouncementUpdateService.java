package com.tastyhouse.application.event.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.event.model.EventAnnouncement;
import com.tastyhouse.domain.event.vo.EventId;
import com.tastyhouse.application.event.port.in.EventAnnouncementUpdateCommand;
import com.tastyhouse.application.event.port.in.EventAnnouncementUpdateUseCase;
import com.tastyhouse.application.event.port.out.write.EventAnnouncementPersistencePort;
import com.tastyhouse.application.shared.exception.AdminErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;

@Service
@Transactional
class EventAnnouncementUpdateService implements EventAnnouncementUpdateUseCase {

    private final EventAnnouncementPersistencePort eventAnnouncementPersistencePort;

    public EventAnnouncementUpdateService(EventAnnouncementPersistencePort eventAnnouncementPersistencePort) {
        this.eventAnnouncementPersistencePort = eventAnnouncementPersistencePort;
    }

    @Override
    public void updateAnnouncement(EventAnnouncementUpdateCommand command) {
        EventId eventId = EventId.of(command.eventId());
        EventAnnouncement announcement = eventAnnouncementPersistencePort.findByEventId(eventId)
            .orElseThrow(() -> new ResourceNotFoundException(AdminErrorCode.EVENT_ANNOUNCEMENT_NOT_FOUND));

        announcement.update(command.name(), command.content(), command.announcedAt());
        eventAnnouncementPersistencePort.save(announcement);
    }
}
