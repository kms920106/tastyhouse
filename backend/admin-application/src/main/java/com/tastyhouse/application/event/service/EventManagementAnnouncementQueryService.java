package com.tastyhouse.application.event.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.event.vo.EventId;
import com.tastyhouse.application.event.port.in.EventManagementAnnouncementQueryUseCase;
import com.tastyhouse.application.event.port.out.EventAnnouncementResult;
import com.tastyhouse.application.event.port.out.EventManagementQueryPort;
import com.tastyhouse.application.shared.exception.AdminErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;

@Service
@Transactional(readOnly = true)
class EventManagementAnnouncementQueryService implements EventManagementAnnouncementQueryUseCase {

    private final EventManagementQueryPort eventManagementQueryPort;

    public EventManagementAnnouncementQueryService(EventManagementQueryPort eventManagementQueryPort) {
        this.eventManagementQueryPort = eventManagementQueryPort;
    }

    @Override
    public EventAnnouncementResult getAnnouncement(Long id) {
        return eventManagementQueryPort.findAnnouncementByEventId(EventId.of(id).value())
            .orElseThrow(() -> new ResourceNotFoundException(AdminErrorCode.EVENT_ANNOUNCEMENT_NOT_FOUND));
    }
}
