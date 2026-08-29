package com.tastyhouse.webapi.event;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.event.model.EventStatus;
import com.tastyhouse.domain.event.vo.EventId;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.shared.page.PageQuery;

import com.tastyhouse.apicommon.common.PaginationResponse;
import com.tastyhouse.infrastructure.event.query.EventAnnouncementResult;
import com.tastyhouse.infrastructure.event.query.EventDetailResult;
import com.tastyhouse.infrastructure.event.query.EventListItemResult;
import com.tastyhouse.infrastructure.event.query.EventQueryDao;
import com.tastyhouse.webapi.event.response.EventAnnouncementListItemResponse;
import com.tastyhouse.webapi.event.response.EventDetailResponse;
import com.tastyhouse.webapi.event.response.EventListItemResponse;

/**
 * 이벤트 조회 서비스(web).
 *
 * <p>infra read 어댑터({@link EventQueryDao})만 주입해 조회하고 Response를 조립한다(패턴 2/3). 이벤트는
 * 회원이 변경하는 리소스가 아니라 web 쪽은 command 없이 QueryService만 둔다.
 *
 * <p>이미지 URL은 DAO가 완성해 주므로 여기서는 파일을 알지 않고 값을 그대로 응답에 전달한다.
 */
@Service
@Transactional(readOnly = true)
public class EventQueryService {

    private final EventQueryDao eventQueryDao;

    public EventQueryService(EventQueryDao eventQueryDao) {
        this.eventQueryDao = eventQueryDao;
    }

    public PaginationResponse<EventListItemResponse> getEventList(String status, int page, int size) {
        PageQuery pageQuery = PageQuery.of(page, size);
        return PaginationResponse.from(eventQueryDao.findEventListItemsByStatus(EventStatus.from(status), pageQuery)
            .map(this::toEventListItemResponse));
    }

    public EventDetailResponse getEventDetail(Long eventId) {
        EventDetailResult detail = eventQueryDao.findEventBannerById(EventId.of(eventId))
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.EVENT_NOT_FOUND));

        return EventDetailResponse.from(detail.bannerUrl());
    }

    public PaginationResponse<EventAnnouncementListItemResponse> getEventAnnouncementList(int page, int size) {
        PageQuery pageQuery = PageQuery.of(page, size);
        return PaginationResponse.from(eventQueryDao.findAnnouncements(pageQuery)
            .map(this::toEventAnnouncementListItemResponse));
    }

    private EventListItemResponse toEventListItemResponse(EventListItemResult dto) {
        return EventListItemResponse.from(
            dto.eventId(),
            dto.name(),
            dto.thumbnailUrl(),
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
