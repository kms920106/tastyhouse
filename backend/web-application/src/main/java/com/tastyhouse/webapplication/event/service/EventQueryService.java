package com.tastyhouse.webapplication.event.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.event.model.EventStatus;
import com.tastyhouse.domain.event.vo.EventId;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.shared.page.PageQuery;

import com.tastyhouse.apicommon.common.PaginationResponse;
import com.tastyhouse.application.event.port.out.EventAnnouncementResult;
import com.tastyhouse.application.event.port.out.EventDetailResult;
import com.tastyhouse.application.event.port.out.EventListItemResult;
import com.tastyhouse.application.event.port.out.EventQueryPort;
import com.tastyhouse.webapplication.event.port.in.EventQueryUseCase;
import com.tastyhouse.webapplication.event.response.EventAnnouncementListItemResponse;
import com.tastyhouse.webapplication.event.response.EventDetailResponse;
import com.tastyhouse.webapplication.event.response.EventListItemResponse;

/**
 * 이벤트 조회 서비스(web).
 *
 * <p>읽기 포트({@link EventQueryPort})만 주입해 조회하고 Response를 조립한다(패턴 2/3). 이벤트는
 * 회원이 변경하는 리소스가 아니라 web 쪽은 command 없이 QueryService만 둔다.
 *
 * <p>이미지 URL은 DAO가 완성해 주므로 여기서는 파일을 알지 않고 값을 그대로 응답에 전달한다.
 */
@Service
@Transactional(readOnly = true)
public class EventQueryService implements EventQueryUseCase {

    private final EventQueryPort eventQueryPort;

    public EventQueryService(EventQueryPort eventQueryPort) {
        this.eventQueryPort = eventQueryPort;
    }

    @Override
    public PaginationResponse<EventListItemResponse> getEventList(String status, int page, int size) {
        PageQuery pageQuery = PageQuery.of(page, size);
        return PaginationResponse.from(eventQueryPort.findEventListItemsByStatus(EventStatus.from(status), pageQuery)
            .map(this::toEventListItemResponse));
    }

    @Override
    public EventDetailResponse getEventDetail(Long eventId) {
        EventDetailResult detail = eventQueryPort.findEventBannerById(EventId.of(eventId))
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.EVENT_NOT_FOUND));

        return EventDetailResponse.from(detail.bannerUrl());
    }

    @Override
    public PaginationResponse<EventAnnouncementListItemResponse> getEventAnnouncementList(int page, int size) {
        PageQuery pageQuery = PageQuery.of(page, size);
        return PaginationResponse.from(eventQueryPort.findAnnouncements(pageQuery)
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
