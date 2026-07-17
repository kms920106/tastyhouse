package com.tastyhouse.core.domain.event.application;

import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.core.domain.event.domain.model.Event;
import com.tastyhouse.core.domain.event.domain.model.EventAnnouncement;
import com.tastyhouse.core.domain.event.domain.model.EventStatus;
import com.tastyhouse.core.domain.event.domain.model.EventWinner;
import com.tastyhouse.core.domain.event.domain.repository.EventAnnouncementRepository;
import com.tastyhouse.core.domain.event.domain.repository.EventRepository;
import com.tastyhouse.core.domain.event.domain.repository.EventWinnerRepository;
import com.tastyhouse.core.domain.event.domain.vo.EventId;
import com.tastyhouse.core.domain.event.application.dto.EventDetailDto;
import com.tastyhouse.core.domain.event.application.dto.EventListItemDto;
import com.tastyhouse.core.domain.event.application.dto.EventManagementDetailDto;
import com.tastyhouse.core.domain.event.application.dto.EventManagementListItemDto;
import com.tastyhouse.core.domain.event.application.dto.EventSearchCondition;
import com.tastyhouse.core.exception.EntityNotFoundException;
import com.tastyhouse.core.exception.ErrorCode;
import com.tastyhouse.core.shared.page.PageQuery;
import com.tastyhouse.core.shared.page.PageResult;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class EventQueryService {

    private final EventRepository eventRepository;
    private final EventAnnouncementRepository eventAnnouncementRepository;
    private final EventWinnerRepository eventWinnerRepository;

    public PageResult<EventAnnouncement> findAllEventAnnouncements(int page, int size) {
        PageQuery pageQuery = PageQuery.of(page, size);
        return eventAnnouncementRepository.findAllOrderByAnnouncedAtDesc(pageQuery);
    }

    public PageResult<EventListItemDto> findEventListItemsByStatus(EventStatus status, int page, int size) {
        PageQuery pageQuery = PageQuery.of(page, size);
        return eventRepository.findEventListItemsByStatus(status, pageQuery);
    }

    public Optional<EventDetailDto> findEventDetailById(EventId eventId) {
        return eventRepository.findEventDetailById(eventId);
    }

    public PageResult<EventManagementListItemDto> findAllEvents(EventSearchCondition condition, int page, int size) {
        PageQuery pageQuery = PageQuery.of(page, size);
        return eventRepository.findAllEvents(condition, pageQuery);
    }

    public EventManagementDetailDto findAdminDetailById(EventId eventId) {
        Event event = eventRepository.findById(eventId)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.EVENT_NOT_FOUND));
        return EventManagementDetailDto.from(event);
    }

    public Optional<EventAnnouncement> findAnnouncementByEventId(EventId eventId) {
        return eventAnnouncementRepository.findByEventId(eventId);
    }

    public List<EventWinner> findWinnersByEventId(EventId eventId) {
        return eventWinnerRepository.findByEventIdOrderByRankNo(eventId);
    }
}
