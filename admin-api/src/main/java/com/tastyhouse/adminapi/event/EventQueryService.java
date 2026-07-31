package com.tastyhouse.adminapi.event;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.event.domain.model.EventStatus;
import com.tastyhouse.domain.event.domain.vo.EventId;
import com.tastyhouse.domain.exception.EntityNotFoundException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.infrastructure.event.query.EventAnnouncementResult;
import com.tastyhouse.infrastructure.event.query.EventManagementDetailResult;
import com.tastyhouse.infrastructure.event.query.EventManagementListItemResult;
import com.tastyhouse.infrastructure.event.query.EventQueryDao;
import com.tastyhouse.infrastructure.event.query.EventSearchCondition;
import com.tastyhouse.infrastructure.event.query.EventWinnerResult;
import com.tastyhouse.adminapi.common.PaginationResponse;
import com.tastyhouse.adminapi.event.response.EventAnnouncementResponse;
import com.tastyhouse.adminapi.event.response.EventDetailResponse;
import com.tastyhouse.adminapi.event.response.EventListItemResponse;
import com.tastyhouse.adminapi.event.response.EventWinnerResponse;
import com.tastyhouse.adminapi.file.FileService;
import com.tastyhouse.adminapi.file.response.FileResponse;

/**
 * 이벤트 관리 조회 서비스(admin).
 *
 * <p>infra read 어댑터({@link EventQueryDao})만 주입해 조회하고 Response를 조립한다(패턴 2/3). 도메인
 * write 포트를 주입하지 않으므로 조회 경로가 도메인 모델을 거치지 않는다.
 *
 * <p>파일 URL 조립은 두 경로로 나뉜다 — 목록은 DAO가 join으로 함께 가져온 파일명·경로를 그대로 쓰고,
 * 상세는 파일 ID만 있으므로 {@link FileService}로 파일을 조회해 URL을 만든다.
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class EventQueryService {

    private final EventQueryDao eventQueryDao;
    private final FileService fileService;

    public PaginationResponse<EventListItemResponse> getEvents(String name, String status, int page, int size) {
        EventStatus eventStatus = status == null ? null : EventStatus.from(status);
        EventSearchCondition condition = EventSearchCondition.of(name, eventStatus);
        PageQuery pageQuery = PageQuery.of(page, size);

        PageResult<EventListItemResponse> pageResult = eventQueryDao.findAllEvents(condition, pageQuery)
            .map(this::toEventListItemResponse);
        return PaginationResponse.from(pageResult);
    }

    public EventDetailResponse getEvent(Long id) {
        EventManagementDetailResult detail = eventQueryDao.findEventDetailById(EventId.of(id))
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.EVENT_NOT_FOUND));
        return toEventDetailResponse(detail);
    }

    public EventAnnouncementResponse getAnnouncement(Long id) {
        EventAnnouncementResult announcement = eventQueryDao.findAnnouncementByEventId(EventId.of(id))
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.EVENT_ANNOUNCEMENT_NOT_FOUND));
        return toEventAnnouncementResponse(announcement);
    }

    public List<EventWinnerResponse> getWinners(Long id) {
        return eventQueryDao.findWinnersByEventId(EventId.of(id)).stream()
            .map(this::toEventWinnerResponse)
            .toList();
    }

    private EventListItemResponse toEventListItemResponse(EventManagementListItemResult dto) {
        FileResponse file = toFileResponse(dto.thumbnailImageFileId(), dto.thumbnailFileName(), dto.thumbnailFilePath());
        return EventListItemResponse.from(
            dto.id(),
            dto.name(),
            dto.status().name(),
            file,
            dto.startAt(),
            dto.endAt()
        );
    }

    private EventDetailResponse toEventDetailResponse(EventManagementDetailResult dto) {
        FileResponse thumbnailFile = toFileResponse(dto.thumbnailImageFileId());
        FileResponse bannerFile = toFileResponse(dto.bannerImageFileId());
        return EventDetailResponse.from(
            dto.id(),
            dto.name(),
            dto.description(),
            dto.subtitle(),
            thumbnailFile,
            bannerFile,
            dto.contentHtml(),
            dto.status().name(),
            dto.startAt(),
            dto.endAt(),
            dto.createdAt(),
            dto.updatedAt()
        );
    }

    private EventAnnouncementResponse toEventAnnouncementResponse(EventAnnouncementResult dto) {
        return EventAnnouncementResponse.from(
            dto.id(),
            dto.eventId(),
            dto.name(),
            dto.content(),
            dto.announcedAt()
        );
    }

    private EventWinnerResponse toEventWinnerResponse(EventWinnerResult dto) {
        return EventWinnerResponse.from(
            dto.id(),
            dto.eventId(),
            dto.rankNo(),
            dto.winnerName(),
            dto.phoneNumber(),
            dto.announcedAt()
        );
    }

    /**
     * 목록용 — DAO가 join으로 함께 가져온 파일명·경로로 조립한다(추가 조회 없음).
     */
    private FileResponse toFileResponse(Long fileId, String fileName, String filePath) {
        if (fileId == null) {
            return null;
        }
        return FileResponse.of(fileId, fileName, fileService.getUrlByPath(filePath));
    }

    /**
     * 상세용 — 파일 ID만 있으므로 파일을 조회해 파일명·URL을 얻는다.
     */
    private FileResponse toFileResponse(Long fileId) {
        if (fileId == null) {
            return null;
        }
        FileResponse fileResponse = fileService.findFileResponse(fileId);
        if (fileResponse == null) {
            throw new EntityNotFoundException(ErrorCode.FILE_NOT_FOUND);
        }
        return fileResponse;
    }
}
