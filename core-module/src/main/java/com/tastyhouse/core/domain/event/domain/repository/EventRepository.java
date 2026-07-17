package com.tastyhouse.core.domain.event.domain.repository;

import java.util.Optional;

import com.tastyhouse.core.domain.event.domain.model.Event;
import com.tastyhouse.core.domain.event.domain.model.EventStatus;
import com.tastyhouse.core.domain.event.domain.vo.EventId;
import com.tastyhouse.core.domain.event.application.dto.EventSearchCondition;
import com.tastyhouse.core.domain.event.application.dto.result.EventDetailResult;
import com.tastyhouse.core.domain.event.application.dto.result.EventListItemResult;
import com.tastyhouse.core.domain.event.application.dto.result.EventManagementListItemResult;
import com.tastyhouse.core.shared.page.PageQuery;
import com.tastyhouse.core.shared.page.PageResult;

public interface EventRepository {

    PageResult<EventListItemResult> findEventListItemsByStatus(EventStatus status, PageQuery pageQuery);

    Optional<EventDetailResult> findEventDetailById(EventId eventId);

    Optional<Event> findById(EventId eventId);

    PageResult<EventManagementListItemResult> findAllEvents(EventSearchCondition condition, PageQuery pageQuery);

    Event save(Event event);
}
