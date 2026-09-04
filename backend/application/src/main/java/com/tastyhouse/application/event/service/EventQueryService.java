package com.tastyhouse.application.event.service;

import com.tastyhouse.application.shared.marker.WebApp;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.event.model.EventStatus;
import com.tastyhouse.domain.event.vo.EventId;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;

import com.tastyhouse.application.event.port.out.EventAnnouncementResult;
import com.tastyhouse.application.event.port.out.EventDetailResult;
import com.tastyhouse.application.event.port.out.EventListItemResult;
import com.tastyhouse.application.event.port.out.EventQueryPort;
import com.tastyhouse.application.event.port.in.EventQueryUseCase;

/**
 * 이벤트 조회 서비스(web).
 *
 * <p>읽기 포트({@link EventQueryPort})만 주입해 조회 결과를 그대로 반환한다(패턴 2/3). 응답 조립은
 * web-api 컨트롤러가 Response의 {@code from}으로 수행한다. 이벤트는 회원이 변경하는 리소스가 아니라
 * web 쪽은 command 없이 QueryService만 둔다.
 *
 * <p>이미지 URL은 DAO가 완성해 주므로 여기서는 파일을 알지 않고 값을 그대로 전달한다.
 */
@Service
@WebApp
@Transactional(readOnly = true)
public class EventQueryService implements EventQueryUseCase {

    private final EventQueryPort eventQueryPort;

    public EventQueryService(EventQueryPort eventQueryPort) {
        this.eventQueryPort = eventQueryPort;
    }

    @Override
    public PageResult<EventListItemResult> getEventList(String status, int page, int size) {
        PageQuery pageQuery = PageQuery.of(page, size);
        return eventQueryPort.findEventListItemsByStatus(EventStatus.from(status), pageQuery);
    }

    @Override
    public EventDetailResult getEventDetail(Long eventId) {
        return eventQueryPort.findEventBannerById(EventId.of(eventId))
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.EVENT_NOT_FOUND));
    }

    @Override
    public PageResult<EventAnnouncementResult> getEventAnnouncementList(int page, int size) {
        PageQuery pageQuery = PageQuery.of(page, size);
        return eventQueryPort.findAnnouncements(pageQuery);
    }
}
