package com.tastyhouse.adminapi.event.application.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.event.model.EventStatus;
import com.tastyhouse.domain.event.vo.EventId;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.infrastructure.event.query.EventAnnouncementResult;
import com.tastyhouse.infrastructure.event.query.EventManagementDetailResult;
import com.tastyhouse.infrastructure.event.query.EventManagementListItemResult;
import com.tastyhouse.infrastructure.event.query.EventQueryDao;
import com.tastyhouse.infrastructure.event.query.EventSearchCondition;
import com.tastyhouse.infrastructure.event.query.EventWinnerResult;
import com.tastyhouse.apicommon.common.PaginationResponse;
import com.tastyhouse.adminapi.event.adapter.in.web.response.EventAnnouncementResponse;
import com.tastyhouse.adminapi.event.adapter.in.web.response.EventDetailResponse;
import com.tastyhouse.adminapi.event.adapter.in.web.response.EventListItemResponse;
import com.tastyhouse.adminapi.event.adapter.in.web.response.EventWinnerResponse;
import com.tastyhouse.adminapi.file.response.FileResponse;
import com.tastyhouse.adminapi.event.application.port.in.EventQueryUseCase;

/**
 * 이벤트 관리 조회 서비스(admin).
 *
 * <p>infra read 어댑터({@link EventQueryDao})만 주입해 조회하고 Response를 조립한다(패턴 2/3). 도메인
 * write 포트를 주입하지 않으므로 조회 경로가 도메인 모델을 거치지 않는다.
 *
 * <p>파일 URL 조립은 DAO가 join으로 함께 파일명·URL까지 완성해 주므로(목록·상세 모두) 이 서비스는
 * 추가 조회 없이 그대로 조립만 한다.
 */
@Service
@Transactional(readOnly = true)
public class EventQueryService implements EventQueryUseCase {

    private final EventQueryDao eventQueryDao;

    public EventQueryService(EventQueryDao eventQueryDao) {
        this.eventQueryDao = eventQueryDao;
    }

    @Override
    public PaginationResponse<EventListItemResponse> getEvents(String name, String status, int page, int size) {
        EventStatus eventStatus = status == null ? null : EventStatus.from(status);
        EventSearchCondition condition = EventSearchCondition.of(name, eventStatus);
        PageQuery pageQuery = PageQuery.of(page, size);

        PageResult<EventListItemResponse> pageResult = eventQueryDao.findAllEvents(condition, pageQuery)
            .map(this::toEventListItemResponse);
        return PaginationResponse.from(pageResult);
    }

    @Override
    public EventDetailResponse getEvent(Long id) {
        EventManagementDetailResult detail = eventQueryDao.findEventDetailById(EventId.of(id))
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.EVENT_NOT_FOUND));
        return toEventDetailResponse(detail);
    }

    @Override
    public EventAnnouncementResponse getAnnouncement(Long id) {
        EventAnnouncementResult announcement = eventQueryDao.findAnnouncementByEventId(EventId.of(id))
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.EVENT_ANNOUNCEMENT_NOT_FOUND));
        return toEventAnnouncementResponse(announcement);
    }

    @Override
    public List<EventWinnerResponse> getWinners(Long id) {
        return eventQueryDao.findWinnersByEventId(EventId.of(id)).stream()
            .map(this::toEventWinnerResponse)
            .toList();
    }

    private EventListItemResponse toEventListItemResponse(EventManagementListItemResult dto) {
        FileResponse file = toFileResponse(dto.thumbnailImageFileId(), dto.thumbnailFileName(), dto.thumbnailUrl());
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
        FileResponse thumbnailFile = toEventDetailFileResponse(dto.thumbnailImageFileId(), dto.thumbnailFileName(), dto.thumbnailUrl());
        FileResponse bannerFile = toEventDetailFileResponse(dto.bannerImageFileId(), dto.bannerFileName(), dto.bannerUrl());
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
     * 목록용 — DAO가 join으로 함께 가져온 파일명·URL로 조립한다(추가 조회 없음). fileId가 없으면(파일
     * 미등록) {@code null}을 그대로 반환한다.
     */
    private FileResponse toFileResponse(Long fileId, String fileName, String imageUrl) {
        if (fileId == null) {
            return null;
        }
        return FileResponse.of(fileId, fileName, imageUrl);
    }

    /**
     * 상세용 — DAO가 join으로 함께 가져온 파일명·URL로 조립한다(추가 조회 없음). fileId가 없으면(파일
     * 미등록) {@code null}을 그대로 반환하되, fileId는 있는데 left join이 URL을 못 찾았다면(참조 무결성
     * 깨짐) 과거 {@code fileService.findFileResponse} 호출 시의 {@code FILE_NOT_FOUND} 동작을 그대로
     * 보존한다 — 썸네일·배너는 필수 자산이므로 조용히 null을 내려보내지 않는다.
     */
    private FileResponse toEventDetailFileResponse(Long fileId, String fileName, String imageUrl) {
        if (fileId == null) {
            return null;
        }
        if (imageUrl == null) {
            throw new ResourceNotFoundException(ErrorCode.FILE_NOT_FOUND);
        }
        return FileResponse.of(fileId, fileName, imageUrl);
    }
}
