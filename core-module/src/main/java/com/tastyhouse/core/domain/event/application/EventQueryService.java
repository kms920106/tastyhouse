package com.tastyhouse.core.domain.event.application;

import com.tastyhouse.core.domain.event.application.dto.EventDetailDto;
import com.tastyhouse.core.domain.event.application.dto.EventListItemDto;
import com.tastyhouse.core.domain.event.domain.model.EventAnnouncement;
import com.tastyhouse.core.domain.event.domain.model.EventStatus;
import com.tastyhouse.core.domain.event.domain.repository.EventAnnouncementRepository;
import com.tastyhouse.core.domain.event.domain.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class EventQueryService {

    private final EventRepository eventRepository;
    private final EventAnnouncementRepository eventAnnouncementRepository;

    public Page<EventAnnouncement> findAllEventAnnouncements(int page, int size) {
        return eventAnnouncementRepository.findAllOrderByAnnouncedAtDesc(PageRequest.of(page, size));
    }

    public Page<EventListItemDto> findEventListItemsByStatus(EventStatus status, int page, int size) {
        return eventRepository.findEventListItemsByStatus(status, PageRequest.of(page, size));
    }

    public Optional<EventDetailDto> findEventDetailById(Long eventId) {
        return eventRepository.findEventDetailById(eventId);
    }
}
