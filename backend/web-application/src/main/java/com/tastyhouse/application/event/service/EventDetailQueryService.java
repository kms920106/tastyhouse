package com.tastyhouse.application.event.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.event.vo.EventId;
import com.tastyhouse.application.event.port.in.EventDetailQueryUseCase;
import com.tastyhouse.application.event.port.out.EventDetailResult;
import com.tastyhouse.application.event.port.out.EventQueryPort;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;

@Service
@Transactional(readOnly = true)
class EventDetailQueryService implements EventDetailQueryUseCase {

    private final EventQueryPort eventQueryPort;

    public EventDetailQueryService(EventQueryPort eventQueryPort) {
        this.eventQueryPort = eventQueryPort;
    }

    @Override
    public EventDetailResult getEventDetail(Long eventId) {
        return eventQueryPort.findEventBannerById(EventId.of(eventId).value())
            .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.EVENT_NOT_FOUND));
    }
}
