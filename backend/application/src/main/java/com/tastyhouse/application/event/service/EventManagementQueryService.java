package com.tastyhouse.application.event.service;

import com.tastyhouse.application.shared.marker.AdminApp;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.event.model.EventStatus;
import com.tastyhouse.domain.event.vo.EventId;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.application.event.port.out.EventAnnouncementResult;
import com.tastyhouse.application.event.port.out.EventManagementDetailResult;
import com.tastyhouse.application.event.port.out.EventManagementListItemResult;
import com.tastyhouse.application.event.port.out.EventManagementQueryPort;
import com.tastyhouse.application.event.port.out.EventSearchCondition;
import com.tastyhouse.application.event.port.out.EventWinnerResult;
import com.tastyhouse.application.event.port.in.EventManagementQueryUseCase;

@Service
@AdminApp
@Transactional(readOnly = true)
public class EventManagementQueryService implements EventManagementQueryUseCase {

    private final EventManagementQueryPort eventManagementQueryPort;

    public EventManagementQueryService(EventManagementQueryPort eventManagementQueryPort) {
        this.eventManagementQueryPort = eventManagementQueryPort;
    }

    @Override
    public PageResult<EventManagementListItemResult> getEvents(String name, String status, int page, int size) {
        EventStatus eventStatus = status == null ? null : EventStatus.from(status);
        EventSearchCondition condition = EventSearchCondition.of(name, eventStatus);
        PageQuery pageQuery = PageQuery.of(page, size);
        return eventManagementQueryPort.findAllEvents(condition, pageQuery);
    }

    @Override
    public EventManagementDetailResult getEvent(Long id) {
        return eventManagementQueryPort.findEventDetailById(EventId.of(id))
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.EVENT_NOT_FOUND));
    }

    @Override
    public EventAnnouncementResult getAnnouncement(Long id) {
        return eventManagementQueryPort.findAnnouncementByEventId(EventId.of(id))
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.EVENT_ANNOUNCEMENT_NOT_FOUND));
    }

    @Override
    public List<EventWinnerResult> getWinners(Long id) {
        return eventManagementQueryPort.findWinnersByEventId(EventId.of(id));
    }
}
