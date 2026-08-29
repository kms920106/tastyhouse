package com.tastyhouse.adminapi.event.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.adminapi.event.application.port.in.EventAnnouncementCreateCommand;
import com.tastyhouse.adminapi.event.application.port.in.EventAnnouncementUpdateCommand;
import com.tastyhouse.adminapi.event.application.port.in.EventCommandUseCase;
import com.tastyhouse.adminapi.event.application.port.in.EventCreateCommand;
import com.tastyhouse.adminapi.event.application.port.in.EventDeleteCommand;
import com.tastyhouse.adminapi.event.application.port.in.EventUpdateCommand;
import com.tastyhouse.adminapi.event.application.port.in.EventWinnerCreateCommand;
import com.tastyhouse.adminapi.event.application.port.in.EventWinnerDeleteCommand;
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

/**
 * 이벤트 관리 명령 서비스(admin).
 *
 * <p>이벤트 CRUD·당첨자 등록/삭제·발표 등록/수정은 모두 단일 애그리거트({@code Event} /
 * {@code EventWinner} / {@code EventAnnouncement}) 조작이므로 write 포트를 직접 주입해 이 서비스가
 * 처리한다(분류 A). 세 포트를 함께 주입하지만 한 트랜잭션에서 두 종류 이상을 함께 save하는 경로는 없다 —
 * 당첨자·발표 생성 시의 {@code eventRepository.findById}는 "대상 이벤트가 존재하는가"를 확인하는 선행
 * 검증(로드만, save 없음)이므로 도메인 서비스로 하강시킬 원자 다중-save가 아니다.
 *
 * <p>세 도메인 모델은 순수 POJO라 더티 체킹이 없으므로 변경 후 명시적으로 {@code save}를 호출한다.
 * HTTP 경계에서 받은 {@code Long}·{@code String}은 이 계층에서 {@code EventId}·{@code EventStatus}로
 * 승격한다.
 */
@Service
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

    /**
     * 이벤트의 당첨자 발표를 등록한다. 이벤트당 1개만 허용하므로 중복 등록을 막는다.
     */
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

    /**
     * 당첨자를 삭제한다(soft delete). 당첨자 ID가 전역 유니크 PK라 이벤트 소속 검증 없이 단독으로
     * 대상을 특정한다(컨트롤러 경로도 평탄화되어 있다).
     */
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
