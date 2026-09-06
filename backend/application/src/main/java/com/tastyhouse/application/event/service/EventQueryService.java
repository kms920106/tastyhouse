package com.tastyhouse.application.event.service;

import com.tastyhouse.application.shared.marker.WebApp;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.event.model.EventStatus;
import com.tastyhouse.domain.event.vo.EventId;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;

import com.tastyhouse.application.event.port.out.EventAnnouncementResult;
import com.tastyhouse.application.event.port.out.EventDetailResult;
import com.tastyhouse.application.event.port.out.EventListItemResult;
import com.tastyhouse.application.event.port.out.EventQueryPort;
import com.tastyhouse.application.event.port.in.EventQueryUseCase;

@Service
@WebApp
@Transactional(readOnly = true)
public class EventQueryService implements EventQueryUseCase {

    private final EventQueryPort eventQueryPort;

    public EventQueryService(EventQueryPort eventQueryPort) {
        this.eventQueryPort = eventQueryPort;
    }

    @Override
    public PageResult<EventListItemResult> getEventList(String status, int page, int size) {
        PageQuery pageQuery = PageQuery.of(page, size);
        return eventQueryPort.findEventListItemsByStatus(EventStatus.from(status), pageQuery);
    }

    @Override
    public EventDetailResult getEventDetail(Long eventId) {
        return eventQueryPort.findEventBannerById(EventId.of(eventId))
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.EVENT_NOT_FOUND));
    }

    @Override
    public PageResult<EventAnnouncementResult> getEventAnnouncementList(int page, int size) {
        PageQuery pageQuery = PageQuery.of(page, size);
        return eventQueryPort.findAnnouncements(pageQuery);
    }
}
