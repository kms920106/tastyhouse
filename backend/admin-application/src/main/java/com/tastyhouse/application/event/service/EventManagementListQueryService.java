package com.tastyhouse.application.event.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.event.model.EventStatus;
import com.tastyhouse.application.event.port.in.EventManagementListQueryUseCase;
import com.tastyhouse.application.event.port.out.EventManagementListItemResult;
import com.tastyhouse.application.event.port.out.EventManagementQueryPort;
import com.tastyhouse.application.event.port.out.EventSearchCondition;
import com.tastyhouse.application.shared.port.out.page.PageQuery;
import com.tastyhouse.application.shared.port.out.page.PageResult;

@Service
@Transactional(readOnly = true)
class EventManagementListQueryService implements EventManagementListQueryUseCase {

    private final EventManagementQueryPort eventManagementQueryPort;

    public EventManagementListQueryService(EventManagementQueryPort eventManagementQueryPort) {
        this.eventManagementQueryPort = eventManagementQueryPort;
    }

    @Override
    public PageResult<EventManagementListItemResult> getEvents(String name, String status, int page, int size) {
        String eventStatus = status == null ? null : EventStatus.from(status).name();
        EventSearchCondition condition = EventSearchCondition.of(name, eventStatus);
        PageQuery pageQuery = PageQuery.of(page, size);
        return eventManagementQueryPort.findAllEvents(condition, pageQuery);
    }
}
