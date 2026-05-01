package com.tastyhouse.core.repository.event;

import com.tastyhouse.core.entity.event.Event;
import com.tastyhouse.core.entity.event.EventAnnouncement;
import com.tastyhouse.core.entity.event.EventStatus;
import com.tastyhouse.core.entity.event.EventType;
import com.tastyhouse.core.entity.event.dto.EventDetailDto;
import com.tastyhouse.core.entity.event.dto.EventListItemDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface EventRepository {

    Optional<Event> findLatestByStatusAndType(EventStatus status, EventType type);

    Page<EventAnnouncement> findAllAnnouncementsOrderByAnnouncedAtDesc(Pageable pageable);

    Page<EventListItemDto> findEventListItemsByStatus(EventStatus status, Pageable pageable);

    Optional<EventDetailDto> findEventDetailById(Long eventId);
}
