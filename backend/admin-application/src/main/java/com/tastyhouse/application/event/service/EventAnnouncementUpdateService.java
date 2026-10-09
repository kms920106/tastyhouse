package com.tastyhouse.application.event.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.event.model.EventAnnouncement;
import com.tastyhouse.domain.event.vo.EventId;
import com.tastyhouse.application.event.port.in.EventAnnouncementUpdateCommand;
import com.tastyhouse.application.event.port.in.EventAnnouncementUpdateUseCase;
import com.tastyhouse.application.event.port.out.write.EventAnnouncementLoadPort;
import com.tastyhouse.application.event.port.out.write.EventAnnouncementSavePort;
import com.tastyhouse.application.shared.exception.AdminErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;

@Service
@Transactional
class EventAnnouncementUpdateService implements EventAnnouncementUpdateUseCase {

    private final EventAnnouncementLoadPort eventAnnouncementLoadPort;
    private final EventAnnouncementSavePort eventAnnouncementSavePort;

    public EventAnnouncementUpdateService(EventAnnouncementLoadPort eventAnnouncementLoadPort, EventAnnouncementSavePort eventAnnouncementSavePort) {
        this.eventAnnouncementLoadPort = eventAnnouncementLoadPort;
        this.eventAnnouncementSavePort = eventAnnouncementSavePort;
    }

    @Override
    public void updateAnnouncement(EventAnnouncementUpdateCommand command) {
        EventId eventId = EventId.of(command.eventId());
        EventAnnouncement announcement = eventAnnouncementLoadPort.findByEventId(eventId)
            .orElseThrow(() -> new ResourceNotFoundException(AdminErrorCode.EVENT_ANNOUNCEMENT_NOT_FOUND));

        announcement.update(command.name(), command.content(), command.announcedAt());
        eventAnnouncementSavePort.save(announcement);
    }
}
