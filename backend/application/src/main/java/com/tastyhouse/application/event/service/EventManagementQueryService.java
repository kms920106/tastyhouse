package com.tastyhouse.application.event.service;

import com.tastyhouse.application.shared.marker.AdminApp;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.event.model.EventStatus;
import com.tastyhouse.domain.event.vo.EventId;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.application.event.port.out.EventAnnouncementResult;
import com.tastyhouse.application.event.port.out.EventManagementDetailResult;
import com.tastyhouse.application.event.port.out.EventManagementListItemResult;
import com.tastyhouse.application.event.port.out.EventManagementQueryPort;
import com.tastyhouse.application.event.port.out.EventSearchCondition;
import com.tastyhouse.application.event.port.out.EventWinnerResult;
import com.tastyhouse.application.event.port.in.EventManagementQueryUseCase;

/**
 * 이벤트 관리 조회 서비스(admin).
 *
 * <p>읽기 포트({@link EventManagementQueryPort})만 주입해 조회한다(패턴 2/3). 도메인 write 포트를
 * 주입하지 않으므로 조회 경로가 도메인 모델을 거치지 않는다.
 *
 * <p>파일 URL 조립은 DAO가 join으로 함께 파일명·URL까지 완성해 주므로(목록·상세 모두) 이 서비스는
 * 추가 조회를 하지 않는다.
 *
 * <p><b>챕터 06</b> — 읽기 포트의 {@code *Result}를 그대로 반환하고 Response로 변환하지 않는다.
 * 표현 계약(@Schema 붙은 Response·{@code FileResponse}·PaginationResponse) 조립은 컨트롤러의 책임이며,
 * 파일 미등록/참조 무결성 분기도 그 Response의 팩토리로 함께 옮겼다.
 */
@Service
@AdminApp
@Transactional(readOnly = true)
public class EventManagementQueryService implements EventManagementQueryUseCase {

    private final EventManagementQueryPort eventManagementQueryPort;

    public EventManagementQueryService(EventManagementQueryPort eventManagementQueryPort) {
        this.eventManagementQueryPort = eventManagementQueryPort;
    }

    @Override
    public PageResult<EventManagementListItemResult> getEvents(String name, String status, int page, int size) {
        EventStatus eventStatus = status == null ? null : EventStatus.from(status);
        EventSearchCondition condition = EventSearchCondition.of(name, eventStatus);
        PageQuery pageQuery = PageQuery.of(page, size);
        return eventManagementQueryPort.findAllEvents(condition, pageQuery);
    }

    @Override
    public EventManagementDetailResult getEvent(Long id) {
        return eventManagementQueryPort.findEventDetailById(EventId.of(id))
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.EVENT_NOT_FOUND));
    }

    @Override
    public EventAnnouncementResult getAnnouncement(Long id) {
        return eventManagementQueryPort.findAnnouncementByEventId(EventId.of(id))
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.EVENT_ANNOUNCEMENT_NOT_FOUND));
    }

    @Override
    public List<EventWinnerResult> getWinners(Long id) {
        return eventManagementQueryPort.findWinnersByEventId(EventId.of(id));
    }
}
