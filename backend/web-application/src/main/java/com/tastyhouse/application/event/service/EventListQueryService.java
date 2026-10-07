package com.tastyhouse.application.event.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.event.model.EventStatus;
import com.tastyhouse.application.event.port.in.EventListQueryUseCase;
import com.tastyhouse.application.event.port.out.EventListItemResult;
import com.tastyhouse.application.event.port.out.EventQueryPort;
import com.tastyhouse.application.shared.port.out.page.PageQuery;
import com.tastyhouse.application.shared.port.out.page.PageResult;

@Service
@Transactional(readOnly = true)
class EventListQueryService implements EventListQueryUseCase {

    private final EventQueryPort eventQueryPort;

    public EventListQueryService(EventQueryPort eventQueryPort) {
        this.eventQueryPort = eventQueryPort;
    }

    @Override
    public PageResult<EventListItemResult> getEventList(String status, int page, int size) {
        PageQuery pageQuery = PageQuery.of(page, size);
        return eventQueryPort.findEventListItemsByStatus(EventStatus.from(status).name(), pageQuery);
    }
}
