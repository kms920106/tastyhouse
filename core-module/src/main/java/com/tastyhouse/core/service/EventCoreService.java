package com.tastyhouse.core.service;

import com.tastyhouse.core.entity.event.Event;
import com.tastyhouse.core.entity.event.EventAnnouncement;
import com.tastyhouse.core.entity.event.EventPrize;
import com.tastyhouse.core.entity.event.EventStatus;
import com.tastyhouse.core.entity.event.EventType;
import com.tastyhouse.core.entity.event.EventWinner;
import com.tastyhouse.core.repository.event.EventAnnouncementJpaRepository;
import com.tastyhouse.core.repository.event.EventJpaRepository;
import com.tastyhouse.core.repository.event.EventPrizeJpaRepository;
import com.tastyhouse.core.repository.event.EventRepository;
import com.tastyhouse.core.repository.event.EventWinnerJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventCoreService {

    private final EventJpaRepository eventJpaRepository;
    private final EventPrizeJpaRepository eventPrizeJpaRepository;
    private final EventWinnerJpaRepository eventWinnerJpaRepository;
    private final EventAnnouncementJpaRepository eventAnnouncementJpaRepository;
    private final EventRepository eventRepository;

    @Transactional(readOnly = true)
    public Optional<Event> findActiveRankingEvent() {
        return eventRepository.findLatestByStatusAndType(EventStatus.ACTIVE, EventType.RANKING);
    }

    @Transactional(readOnly = true)
    public Page<Event> searchEventsByStatus(EventStatus status, int page, int size) {
        return eventRepository.findByStatusOrderByStartAtDesc(status, PageRequest.of(page, size));
    }

    @Transactional(readOnly = true)
    public List<EventPrize> findEventPrizes(Long eventId) {
        return eventRepository.findPrizesByEventIdOrderByPrizeRankAsc(eventId);
    }

    @Transactional
    public Event saveEvent(Event event) {
        return eventJpaRepository.save(event);
    }

    @Transactional
    public EventPrize saveEventPrize(EventPrize eventPrize) {
        return eventPrizeJpaRepository.save(eventPrize);
    }

    @Transactional(readOnly = true)
    public Optional<Event> findEventById(Long eventId) {
        return eventJpaRepository.findById(eventId);
    }

    @Transactional(readOnly = true)
    public List<EventWinner> findEventWinnersByEventId(Long eventId) {
        return eventRepository.findWinnersByEventIdOrderByAnnouncedAtDescRankNoAsc(eventId);
    }

    @Transactional(readOnly = true)
    public List<EventWinner> findAllEventWinners() {
        return eventRepository.findAllWinnersOrderByAnnouncedAtDescRankNoAsc();
    }

    @Transactional
    public EventWinner saveEventWinner(EventWinner eventWinner) {
        return eventWinnerJpaRepository.save(eventWinner);
    }

    @Transactional(readOnly = true)
    public Page<EventAnnouncement> findAllEventAnnouncements(int page, int size) {
        return eventRepository.findAllAnnouncementsOrderByAnnouncedAtDesc(PageRequest.of(page, size));
    }

    @Transactional(readOnly = true)
    public Optional<EventAnnouncement> findEventAnnouncementByEventId(Long eventId) {
        return eventRepository.findAnnouncementByEventId(eventId);
    }

    @Transactional
    public EventAnnouncement saveEventAnnouncement(EventAnnouncement eventAnnouncement) {
        return eventAnnouncementJpaRepository.save(eventAnnouncement);
    }
}
