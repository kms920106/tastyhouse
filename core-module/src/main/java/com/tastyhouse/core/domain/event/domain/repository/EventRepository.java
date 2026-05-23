package com.tastyhouse.core.domain.event.domain.repository;

import com.tastyhouse.core.domain.event.application.dto.EventDetailDto;
import com.tastyhouse.core.domain.event.application.dto.EventListItemDto;
import com.tastyhouse.core.domain.event.domain.model.Event;
import com.tastyhouse.core.domain.event.domain.model.EventStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface EventRepository {

    Optional<Event> findLatestByStatus(EventStatus status);

    Page<EventListItemDto> findEventListItemsByStatus(EventStatus status, Pageable pageable);

    Optional<EventDetailDto> findEventDetailById(Long eventId);
}
