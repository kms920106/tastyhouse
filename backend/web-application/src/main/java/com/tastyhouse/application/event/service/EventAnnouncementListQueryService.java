package com.tastyhouse.application.event.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.event.port.in.EventAnnouncementListQueryUseCase;
import com.tastyhouse.application.event.port.out.EventAnnouncementResult;
import com.tastyhouse.application.event.port.out.EventQueryPort;
import com.tastyhouse.application.shared.port.out.page.PageQuery;
import com.tastyhouse.application.shared.port.out.page.PageResult;

@Service
@Transactional(readOnly = true)
class EventAnnouncementListQueryService implements EventAnnouncementListQueryUseCase {

    private final EventQueryPort eventQueryPort;

    public EventAnnouncementListQueryService(EventQueryPort eventQueryPort) {
        this.eventQueryPort = eventQueryPort;
    }

    @Override
    public PageResult<EventAnnouncementResult> getEventAnnouncementList(int page, int size) {
        PageQuery pageQuery = PageQuery.of(page, size);
        return eventQueryPort.findAnnouncements(pageQuery);
    }
}
