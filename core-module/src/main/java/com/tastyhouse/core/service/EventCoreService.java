package com.tastyhouse.core.service;

import com.tastyhouse.core.entity.event.EventAnnouncement;
import com.tastyhouse.core.entity.event.EventStatus;
import com.tastyhouse.core.entity.event.dto.EventDetailDto;
import com.tastyhouse.core.entity.event.dto.EventListItemDto;
import com.tastyhouse.core.repository.event.EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventCoreService {

    private final EventRepository eventRepository;

    @Transactional(readOnly = true)
    public Page<EventAnnouncement> findAllEventAnnouncements(int page, int size) {
        return eventRepository.findAllAnnouncementsOrderByAnnouncedAtDesc(PageRequest.of(page, size));
    }

    @Transactional(readOnly = true)
    public Page<EventListItemDto> findEventListItemsByStatus(EventStatus status, int page, int size) {
        return eventRepository.findEventListItemsByStatus(status, PageRequest.of(page, size));
    }

    @Transactional(readOnly = true)
    public Optional<EventDetailDto> findEventDetailById(Long eventId) {
        return eventRepository.findEventDetailById(eventId);
    }
}
