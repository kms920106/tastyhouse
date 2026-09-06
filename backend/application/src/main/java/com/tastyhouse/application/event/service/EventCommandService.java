package com.tastyhouse.application.event.service;

import com.tastyhouse.application.shared.marker.AdminApp;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.event.port.in.EventAnnouncementCreateCommand;
import com.tastyhouse.application.event.port.in.EventAnnouncementUpdateCommand;
import com.tastyhouse.application.event.port.in.EventCommandUseCase;
import com.tastyhouse.application.event.port.in.EventCreateCommand;
import com.tastyhouse.application.event.port.in.EventDeleteCommand;
import com.tastyhouse.application.event.port.in.EventUpdateCommand;
import com.tastyhouse.application.event.port.in.EventWinnerCreateCommand;
import com.tastyhouse.application.event.port.in.EventWinnerDeleteCommand;
import com.tastyhouse.domain.event.model.Event;
import com.tastyhouse.domain.event.model.EventAnnouncement;
import com.tastyhouse.domain.event.model.EventStatus;
import com.tastyhouse.domain.event.model.EventWinner;
import com.tastyhouse.domain.event.repository.EventAnnouncementRepository;
import com.tastyhouse.domain.event.repository.EventRepository;
import com.tastyhouse.domain.event.repository.EventWinnerRepository;
import com.tastyhouse.domain.event.vo.EventId;
import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;

@Service
@AdminApp
@Transactional
public class EventCommandService implements EventCommandUseCase {

    private final EventRepository eventRepository;
    private final EventAnnouncementRepository eventAnnouncementRepository;
    private final EventWinnerRepository eventWinnerRepository;

    public EventCommandService(
        EventRepository eventRepository,
        EventAnnouncementRepository eventAnnouncementRepository,
        EventWinnerRepository eventWinnerRepository
    ) {
        this.eventRepository = eventRepository;
        this.eventAnnouncementRepository = eventAnnouncementRepository;
        this.eventWinnerRepository = eventWinnerRepository;
    }

    @Override
    public Long createEvent(EventCreateCommand command) {
        Long thumbnailImageFileId = command.thumbnailImageFileId();
        Long bannerImageFileId = command.bannerImageFileId();

        Event event = Event.of(
            command.name(),
            command.description(),
            command.subtitle(),
            thumbnailImageFileId == null ? null : UploadedFileId.of(thumbnailImageFileId),
            bannerImageFileId == null ? null : UploadedFileId.of(bannerImageFileId),
            command.contentHtml(),
            EventStatus.from(command.status()),
            command.startAt(),
            command.endAt()
        );
        Event saved = eventRepository.save(event);
        return saved.getEventId().value();
    }

    @Override
    public void updateEvent(EventUpdateCommand command) {
        Long thumbnailImageFileId = command.thumbnailImageFileId();
        Long bannerImageFileId = command.bannerImageFileId();
        EventId eventId = EventId.of(command.eventId());
        Event event = findEventOrThrow(eventId);

        event.update(
            command.name(),
            command.description(),
            command.subtitle(),
            thumbnailImageFileId == null ? null : UploadedFileId.of(thumbnailImageFileId),
            bannerImageFileId == null ? null : UploadedFileId.of(bannerImageFileId),
            command.contentHtml(),
            EventStatus.from(command.status()),
            command.startAt(),
            command.endAt()
        );
        eventRepository.save(event);
    }

    @Override
    public void deleteEvent(EventDeleteCommand command) {
        EventId eventId = EventId.of(command.eventId());
        Event event = findEventOrThrow(eventId);

        event.delete();
        eventRepository.save(event);
    }

    @Override
    public Long createAnnouncement(EventAnnouncementCreateCommand command) {
        EventId eventId = EventId.of(command.eventId());
        findEventOrThrow(eventId);

        if (eventAnnouncementRepository.existsByEventId(eventId)) {
            throw new BusinessException(ErrorCode.EVENT_ANNOUNCEMENT_ALREADY_EXISTS);
        }

        EventAnnouncement announcement = EventAnnouncement.of(eventId, command.name(), command.content(), command.announcedAt());
        EventAnnouncement saved = eventAnnouncementRepository.save(announcement);
        return saved.getId();
    }

    @Override
    public void updateAnnouncement(EventAnnouncementUpdateCommand command) {
        EventId eventId = EventId.of(command.eventId());
        EventAnnouncement announcement = eventAnnouncementRepository.findByEventId(eventId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.EVENT_ANNOUNCEMENT_NOT_FOUND));

        announcement.update(command.name(), command.content(), command.announcedAt());
        eventAnnouncementRepository.save(announcement);
    }

    @Override
    public Long createWinner(EventWinnerCreateCommand command) {
        EventId eventId = EventId.of(command.eventId());
        findEventOrThrow(eventId);

        EventWinner winner = EventWinner.of(eventId, command.rankNo(), command.winnerName(), command.phoneNumber(), command.announcedAt());
        EventWinner saved = eventWinnerRepository.save(winner);
        return saved.getId();
    }

    @Override
    public void deleteWinner(EventWinnerDeleteCommand command) {
        EventWinner winner = eventWinnerRepository.findById(command.winnerId())
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.EVENT_WINNER_NOT_FOUND));

        winner.delete();
        eventWinnerRepository.save(winner);
    }

    private Event findEventOrThrow(EventId eventId) {
        return eventRepository.findById(eventId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.EVENT_NOT_FOUND));
    }
}
