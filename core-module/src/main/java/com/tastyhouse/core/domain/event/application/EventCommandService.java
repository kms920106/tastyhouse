package com.tastyhouse.core.domain.event.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.core.domain.event.domain.model.Event;
import com.tastyhouse.core.domain.event.domain.model.EventAnnouncement;
import com.tastyhouse.core.domain.event.domain.model.EventWinner;
import com.tastyhouse.core.domain.event.domain.repository.EventAnnouncementRepository;
import com.tastyhouse.core.domain.event.domain.repository.EventRepository;
import com.tastyhouse.core.domain.event.domain.repository.EventWinnerRepository;
import com.tastyhouse.core.domain.event.domain.vo.EventId;
import com.tastyhouse.core.domain.event.application.dto.command.EventAnnouncementCreateCommand;
import com.tastyhouse.core.domain.event.application.dto.command.EventAnnouncementUpdateCommand;
import com.tastyhouse.core.domain.event.application.dto.command.EventCreateCommand;
import com.tastyhouse.core.domain.event.application.dto.command.EventUpdateCommand;
import com.tastyhouse.core.domain.event.application.dto.command.EventWinnerCreateCommand;
import com.tastyhouse.core.exception.BusinessException;
import com.tastyhouse.core.exception.EntityNotFoundException;
import com.tastyhouse.core.exception.ErrorCode;

@Service
@Transactional
@RequiredArgsConstructor
public class EventCommandService {

    private final EventRepository eventRepository;
    private final EventAnnouncementRepository eventAnnouncementRepository;
    private final EventWinnerRepository eventWinnerRepository;

    public EventId createEvent(EventCreateCommand command) {
        Event event = Event.of(
            command.name(),
            command.description(),
            command.subtitle(),
            command.thumbnailImageFileId(),
            command.bannerImageFileId(),
            command.contentHtml(),
            command.status(),
            command.startAt(),
            command.endAt()
        );
        Event saved = eventRepository.save(event);
        return saved.getEventId();
    }

    public void updateEvent(EventId eventId, EventUpdateCommand command) {
        Event event = eventRepository.findById(eventId)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.EVENT_NOT_FOUND));

        event.update(
            command.name(),
            command.description(),
            command.subtitle(),
            command.thumbnailImageFileId(),
            command.bannerImageFileId(),
            command.contentHtml(),
            command.status(),
            command.startAt(),
            command.endAt()
        );
        eventRepository.save(event);
    }

    public void deleteEvent(EventId eventId) {
        Event event = eventRepository.findById(eventId)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.EVENT_NOT_FOUND));

        event.delete();
        eventRepository.save(event);
    }

    public Long createAnnouncement(EventId eventId, EventAnnouncementCreateCommand command) {
        eventRepository.findById(eventId)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.EVENT_NOT_FOUND));

        if (eventAnnouncementRepository.existsByEventId(eventId)) {
            throw new BusinessException(ErrorCode.EVENT_ANNOUNCEMENT_ALREADY_EXISTS);
        }

        EventAnnouncement announcement = EventAnnouncement.of(
            eventId.value(),
            command.name(),
            command.content(),
            command.announcedAt()
        );
        EventAnnouncement saved = eventAnnouncementRepository.save(announcement);
        return saved.getId();
    }

    public void updateAnnouncement(EventId eventId, EventAnnouncementUpdateCommand command) {
        EventAnnouncement announcement = eventAnnouncementRepository.findByEventId(eventId)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.EVENT_ANNOUNCEMENT_NOT_FOUND));

        announcement.update(
            command.name(),
            command.content(),
            command.announcedAt()
        );
        eventAnnouncementRepository.save(announcement);
    }

    public Long createWinner(EventId eventId, EventWinnerCreateCommand command) {
        eventRepository.findById(eventId)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.EVENT_NOT_FOUND));

        EventWinner winner = EventWinner.of(
            eventId.value(),
            command.rankNo(),
            command.winnerName(),
            command.phoneNumber(),
            command.announcedAt()
        );
        EventWinner saved = eventWinnerRepository.save(winner);
        return saved.getId();
    }

    public void deleteWinner(Long winnerId) {
        EventWinner winner = eventWinnerRepository.findById(winnerId)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.EVENT_WINNER_NOT_FOUND));

        winner.delete();
        eventWinnerRepository.save(winner);
    }
}
