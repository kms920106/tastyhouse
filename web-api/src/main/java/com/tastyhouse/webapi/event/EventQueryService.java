package com.tastyhouse.webapi.event;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.core.domain.event.domain.model.EventStatus;
import com.tastyhouse.core.domain.event.domain.vo.EventId;
import com.tastyhouse.core.exception.EntityNotFoundException;
import com.tastyhouse.core.exception.ErrorCode;
import com.tastyhouse.core.shared.page.PageQuery;
import com.tastyhouse.core.shared.page.PageResult;
import com.tastyhouse.infrastructure.event.query.EventAnnouncementResult;
import com.tastyhouse.infrastructure.event.query.EventDetailResult;
import com.tastyhouse.infrastructure.event.query.EventListItemResult;
import com.tastyhouse.infrastructure.event.query.EventQueryDao;
import com.tastyhouse.webapi.event.response.EventAnnouncementListItemResponse;
import com.tastyhouse.webapi.event.response.EventDetailResponse;
import com.tastyhouse.webapi.event.response.EventListItemResponse;
import com.tastyhouse.webapi.file.FileService;

/**
 * 이벤트 조회 서비스(web).
 *
 * <p>infra read 어댑터({@link EventQueryDao})만 주입해 조회하고 Response를 조립한다(패턴 2/3). 이벤트는
 * 회원이 변경하는 리소스가 아니라 web 쪽은 command 없이 QueryService만 둔다.
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class EventQueryService {

    private final EventQueryDao eventQueryDao;
    private final FileService fileService;

    public PageResult<EventListItemResponse> getEventList(String status, int page, int size) {
        PageQuery pageQuery = PageQuery.of(page, size);
        return eventQueryDao.findEventListItemsByStatus(EventStatus.from(status), pageQuery)
            .map(this::toEventListItemResponse);
    }

    public EventDetailResponse getEventDetail(Long eventId) {
        EventDetailResult detail = eventQueryDao.findEventBannerById(EventId.of(eventId))
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.ENTITY_NOT_FOUND, "이벤트를 찾을 수 없습니다."));

        return EventDetailResponse.from(fileService.getUrlByPath(detail.bannerFilePath()));
    }

    public PageResult<EventAnnouncementListItemResponse> getEventAnnouncementList(int page, int size) {
        PageQuery pageQuery = PageQuery.of(page, size);
        return eventQueryDao.findAnnouncements(pageQuery)
            .map(this::toEventAnnouncementListItemResponse);
    }

    private EventListItemResponse toEventListItemResponse(EventListItemResult dto) {
        return EventListItemResponse.from(
            dto.eventId(),
            dto.name(),
            fileService.getUrlByPath(dto.thumbnailFilePath()),
            dto.startAt(),
            dto.endAt()
        );
    }

    private EventAnnouncementListItemResponse toEventAnnouncementListItemResponse(EventAnnouncementResult dto) {
        return EventAnnouncementListItemResponse.from(
            dto.id(),
            dto.name(),
            dto.content(),
            dto.announcedAt()
        );
    }
}
