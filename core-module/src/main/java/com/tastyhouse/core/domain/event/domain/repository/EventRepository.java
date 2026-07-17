package com.tastyhouse.core.domain.event.domain.repository;

import java.util.Optional;

import com.tastyhouse.core.domain.event.application.dto.EventDetailDto;
import com.tastyhouse.core.domain.event.application.dto.EventListItemDto;
import com.tastyhouse.core.domain.event.application.dto.EventManagementListItemDto;
import com.tastyhouse.core.domain.event.application.dto.EventSearchCondition;
import com.tastyhouse.core.domain.event.domain.model.Event;
import com.tastyhouse.core.domain.event.domain.model.EventStatus;
import com.tastyhouse.core.domain.event.domain.vo.EventId;
import com.tastyhouse.core.shared.page.PageQuery;
import com.tastyhouse.core.shared.page.PageResult;

public interface EventRepository {

    PageResult<EventListItemDto> findEventListItemsByStatus(EventStatus status, PageQuery pageQuery);

    Optional<EventDetailDto> findEventDetailById(EventId eventId);

    Optional<Event> findById(EventId eventId);

    PageResult<EventManagementListItemDto> findAllEvents(EventSearchCondition condition, PageQuery pageQuery);

    Event save(Event event);
}
