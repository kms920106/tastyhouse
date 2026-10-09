package com.tastyhouse.application.event.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.event.model.EventAnnouncement;
import com.tastyhouse.domain.event.vo.EventId;
import com.tastyhouse.application.event.port.in.EventAnnouncementCreateCommand;
import com.tastyhouse.application.event.port.in.EventAnnouncementCreateUseCase;
import com.tastyhouse.application.event.port.out.write.EventAnnouncementLoadPort;
import com.tastyhouse.application.event.port.out.write.EventAnnouncementSavePort;
import com.tastyhouse.application.event.port.out.write.EventLoadPort;
import com.tastyhouse.application.shared.exception.AdminErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;

@Service
@Transactional
class EventAnnouncementCreateService implements EventAnnouncementCreateUseCase {

    private final EventLoadPort eventLoadPort;
    private final EventAnnouncementLoadPort eventAnnouncementLoadPort;
    private final EventAnnouncementSavePort eventAnnouncementSavePort;

    public EventAnnouncementCreateService(
        EventLoadPort eventLoadPort,
        EventAnnouncementLoadPort eventAnnouncementLoadPort,
        EventAnnouncementSavePort eventAnnouncementSavePort
    ) {
        this.eventLoadPort = eventLoadPort;
        this.eventAnnouncementLoadPort = eventAnnouncementLoadPort;
        this.eventAnnouncementSavePort = eventAnnouncementSavePort;
    }

    @Override
    public Long createAnnouncement(EventAnnouncementCreateCommand command) {
        EventId eventId = EventId.of(command.eventId());
        verifyEventExists(eventId);

        if (eventAnnouncementLoadPort.existsByEventId(eventId)) {
            throw new ApplicationException(AdminErrorCode.EVENT_ANNOUNCEMENT_ALREADY_EXISTS);
        }

        EventAnnouncement announcement = EventAnnouncement.of(eventId, command.name(), command.content(), command.announcedAt());
        EventAnnouncement saved = eventAnnouncementSavePort.save(announcement);
        return saved.getId();
    }

    private void verifyEventExists(EventId eventId) {
        eventLoadPort.findById(eventId)
            .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.EVENT_NOT_FOUND));
    }
}
