package com.tastyhouse.core.domain.event.application;

import com.tastyhouse.core.domain.event.application.dto.EventDetailDto;
import com.tastyhouse.core.domain.event.application.dto.EventListItemDto;
import com.tastyhouse.core.domain.event.domain.model.EventAnnouncement;
import com.tastyhouse.core.domain.event.domain.model.EventStatus;
import com.tastyhouse.core.domain.event.domain.repository.EventAnnouncementRepository;
import com.tastyhouse.core.domain.event.domain.repository.EventRepository;
import com.tastyhouse.core.shared.page.PageQuery;
import com.tastyhouse.core.shared.page.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class EventQueryService {

    private final EventRepository eventRepository;
    private final EventAnnouncementRepository eventAnnouncementRepository;

    public PageResult<EventAnnouncement> findAllEventAnnouncements(int page, int size) {
        PageQuery pageQuery = PageQuery.of(page, size);
        return eventAnnouncementRepository.findAllOrderByAnnouncedAtDesc(pageQuery);
    }

    public PageResult<EventListItemDto> findEventListItemsByStatus(EventStatus status, int page, int size) {
        PageQuery pageQuery = PageQuery.of(page, size);
        return eventRepository.findEventListItemsByStatus(status, pageQuery);
    }

    public Optional<EventDetailDto> findEventDetailById(Long eventId) {
        return eventRepository.findEventDetailById(eventId);
    }
}
