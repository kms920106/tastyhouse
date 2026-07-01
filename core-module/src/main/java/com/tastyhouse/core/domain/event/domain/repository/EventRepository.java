package com.tastyhouse.core.domain.event.domain.repository;

import com.tastyhouse.core.domain.event.application.dto.EventDetailDto;
import com.tastyhouse.core.domain.event.application.dto.EventListItemDto;
import com.tastyhouse.core.domain.event.domain.model.EventStatus;
import com.tastyhouse.core.shared.page.PageQuery;
import com.tastyhouse.core.shared.page.PageResult;

import java.util.Optional;

public interface EventRepository {

    PageResult<EventListItemDto> findEventListItemsByStatus(EventStatus status, PageQuery pageQuery);

    Optional<EventDetailDto> findEventDetailById(Long eventId);
}
