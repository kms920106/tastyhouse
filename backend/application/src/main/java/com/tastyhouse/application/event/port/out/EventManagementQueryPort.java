package com.tastyhouse.application.event.port.out;

import java.util.List;
import java.util.Optional;

import com.tastyhouse.domain.event.vo.EventId;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;

/**
 * 이벤트 관리 화면 조회 포트(CQRS query 측 아웃바운드 포트).
 *
 * <p>비노출·종료 이벤트를 포함한 관리 목록·상세와 당첨자 명단을 조회한다. 회원 노출 조회는
 * {@code EventQueryPort}가 소유한다.
 */
public interface EventManagementQueryPort {

    PageResult<EventManagementListItemResult> findAllEvents(EventSearchCondition condition, PageQuery pageQuery);

    Optional<EventManagementDetailResult> findEventDetailById(EventId eventId);

    List<EventWinnerResult> findWinnersByEventId(EventId eventId);

    Optional<EventAnnouncementResult> findAnnouncementByEventId(EventId eventId);
}
