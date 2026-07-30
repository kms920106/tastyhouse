package com.tastyhouse.adminapi.event;

import java.time.LocalDateTime;

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
import com.tastyhouse.core.exception.BusinessException;
import com.tastyhouse.core.exception.EntityNotFoundException;
import com.tastyhouse.core.exception.ErrorCode;

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
@RequiredArgsConstructor
public class EventCommandService {

    private final EventRepository eventRepository;
    private final EventAnnouncementRepository eventAnnouncementRepository;
    private final EventWinnerRepository eventWinnerRepository;

    public Long createEvent(
        String name,
        String description,
        String subtitle,
        Long thumbnailImageFileId,
        Long bannerImageFileId,
        String contentHtml,
        String status,
        LocalDateTime startAt,
        LocalDateTime endAt
    ) {
        Event event = Event.of(
            name,
            description,
            subtitle,
            thumbnailImageFileId,
            bannerImageFileId,
            contentHtml,
            EventStatus.from(status),
            startAt,
            endAt
        );
        Event saved = eventRepository.save(event);
        return saved.getEventId().value();
    }

    public void updateEvent(
        Long id,
        String name,
        String description,
        String subtitle,
        Long thumbnailImageFileId,
        Long bannerImageFileId,
        String contentHtml,
        String status,
        LocalDateTime startAt,
        LocalDateTime endAt
    ) {
        EventId eventId = EventId.of(id);
        Event event = findEventOrThrow(eventId);

        event.update(
            name,
            description,
            subtitle,
            thumbnailImageFileId,
            bannerImageFileId,
            contentHtml,
            EventStatus.from(status),
            startAt,
            endAt
        );
        eventRepository.save(event);
    }

    public void deleteEvent(Long id) {
        EventId eventId = EventId.of(id);
        Event event = findEventOrThrow(eventId);

        event.delete();
        eventRepository.save(event);
    }

    /**
     * 이벤트의 당첨자 발표를 등록한다. 이벤트당 1개만 허용하므로 중복 등록을 막는다.
     */
    public Long createAnnouncement(Long id, String name, String content, LocalDateTime announcedAt) {
        EventId eventId = EventId.of(id);
        findEventOrThrow(eventId);

        if (eventAnnouncementRepository.existsByEventId(eventId)) {
            throw new BusinessException(ErrorCode.EVENT_ANNOUNCEMENT_ALREADY_EXISTS);
        }

        EventAnnouncement announcement = EventAnnouncement.of(eventId.value(), name, content, announcedAt);
        EventAnnouncement saved = eventAnnouncementRepository.save(announcement);
        return saved.getId();
    }

    public void updateAnnouncement(Long id, String name, String content, LocalDateTime announcedAt) {
        EventId eventId = EventId.of(id);
        EventAnnouncement announcement = eventAnnouncementRepository.findByEventId(eventId)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.EVENT_ANNOUNCEMENT_NOT_FOUND));

        announcement.update(name, content, announcedAt);
        eventAnnouncementRepository.save(announcement);
    }

    public Long createWinner(Long id, Integer rankNo, String winnerName, String phoneNumber, LocalDateTime announcedAt) {
        EventId eventId = EventId.of(id);
        findEventOrThrow(eventId);

        EventWinner winner = EventWinner.of(eventId.value(), rankNo, winnerName, phoneNumber, announcedAt);
        EventWinner saved = eventWinnerRepository.save(winner);
        return saved.getId();
    }

    /**
     * 당첨자를 삭제한다(soft delete). 당첨자 ID가 전역 유니크 PK라 이벤트 소속 검증 없이 단독으로
     * 대상을 특정한다(컨트롤러 경로도 평탄화되어 있다).
     */
    public void deleteWinner(Long winnerId) {
        EventWinner winner = eventWinnerRepository.findById(winnerId)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.EVENT_WINNER_NOT_FOUND));

        winner.delete();
        eventWinnerRepository.save(winner);
    }

    private Event findEventOrThrow(EventId eventId) {
        return eventRepository.findById(eventId)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.EVENT_NOT_FOUND));
    }
}
