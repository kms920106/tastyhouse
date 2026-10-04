package com.tastyhouse.application.event.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.event.model.Event;
import com.tastyhouse.domain.event.model.EventAnnouncement;
import com.tastyhouse.domain.event.model.EventStatus;
import com.tastyhouse.domain.event.model.EventWinner;
import com.tastyhouse.domain.event.vo.EventId;
import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.application.event.port.in.EventAnnouncementCreateCommand;
import com.tastyhouse.application.event.port.in.EventAnnouncementUpdateCommand;
import com.tastyhouse.application.event.port.in.EventCommandUseCase;
import com.tastyhouse.application.event.port.in.EventCreateCommand;
import com.tastyhouse.application.event.port.in.EventDeleteCommand;
import com.tastyhouse.application.event.port.in.EventUpdateCommand;
import com.tastyhouse.application.event.port.in.EventWinnerCreateCommand;
import com.tastyhouse.application.event.port.in.EventWinnerDeleteCommand;
import com.tastyhouse.application.event.port.out.write.EventAnnouncementPersistencePort;
import com.tastyhouse.application.event.port.out.write.EventPersistencePort;
import com.tastyhouse.application.event.port.out.write.EventWinnerPersistencePort;
import com.tastyhouse.application.shared.exception.AdminErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;

@Service
@Transactional
class EventCommandService implements EventCommandUseCase {

    private final EventPersistencePort eventPersistencePort;
    private final EventAnnouncementPersistencePort eventAnnouncementPersistencePort;
    private final EventWinnerPersistencePort eventWinnerPersistencePort;

    public EventCommandService(
        EventPersistencePort eventPersistencePort,
        EventAnnouncementPersistencePort eventAnnouncementPersistencePort,
        EventWinnerPersistencePort eventWinnerPersistencePort
    ) {
        this.eventPersistencePort = eventPersistencePort;
        this.eventAnnouncementPersistencePort = eventAnnouncementPersistencePort;
        this.eventWinnerPersistencePort = eventWinnerPersistencePort;
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
        Event saved = eventPersistencePort.save(event);
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
        eventPersistencePort.save(event);
    }

    @Override
    public void deleteEvent(EventDeleteCommand command) {
        EventId eventId = EventId.of(command.eventId());
        Event event = findEventOrThrow(eventId);

        event.delete();
        eventPersistencePort.save(event);
    }

    @Override
    public Long createAnnouncement(EventAnnouncementCreateCommand command) {
        EventId eventId = EventId.of(command.eventId());
        findEventOrThrow(eventId);

        if (eventAnnouncementPersistencePort.existsByEventId(eventId)) {
            throw new ApplicationException(AdminErrorCode.EVENT_ANNOUNCEMENT_ALREADY_EXISTS);
        }

        EventAnnouncement announcement = EventAnnouncement.of(eventId, command.name(), command.content(), command.announcedAt());
        EventAnnouncement saved = eventAnnouncementPersistencePort.save(announcement);
        return saved.getId();
    }

    @Override
    public void updateAnnouncement(EventAnnouncementUpdateCommand command) {
        EventId eventId = EventId.of(command.eventId());
        EventAnnouncement announcement = eventAnnouncementPersistencePort.findByEventId(eventId)
            .orElseThrow(() -> new ResourceNotFoundException(AdminErrorCode.EVENT_ANNOUNCEMENT_NOT_FOUND));

        announcement.update(command.name(), command.content(), command.announcedAt());
        eventAnnouncementPersistencePort.save(announcement);
    }

    @Override
    public Long createWinner(EventWinnerCreateCommand command) {
        EventId eventId = EventId.of(command.eventId());
        findEventOrThrow(eventId);

        EventWinner winner = EventWinner.of(eventId, command.rankNo(), command.winnerName(), command.phoneNumber(), command.announcedAt());
        EventWinner saved = eventWinnerPersistencePort.save(winner);
        return saved.getId();
    }

    @Override
    public void deleteWinner(EventWinnerDeleteCommand command) {
        EventWinner winner = eventWinnerPersistencePort.findById(command.winnerId())
            .orElseThrow(() -> new ResourceNotFoundException(AdminErrorCode.EVENT_WINNER_NOT_FOUND));

        winner.delete();
        eventWinnerPersistencePort.save(winner);
    }

    private Event findEventOrThrow(EventId eventId) {
        return eventPersistencePort.findById(eventId)
            .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.EVENT_NOT_FOUND));
    }
}
