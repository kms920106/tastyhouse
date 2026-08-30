package com.tastyhouse.application.event.port.out;

import java.util.Optional;

import com.tastyhouse.domain.event.model.EventStatus;
import com.tastyhouse.domain.event.vo.EventId;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;

/**
 * 이벤트 회원 노출 조회 포트(CQRS query 측 아웃바운드 포트).
 *
 * <p>진행 상태별 목록·배너와 당첨자 발표 공지를 회원 화면에 노출하는 조회만 다룬다. 이벤트를
 * 등록·검수하는 관리 화면 조회는 {@link EventManagementQueryPort}가 소유한다 — 공유 메서드는 0개다.
 */
public interface EventQueryPort {

    PageResult<EventListItemResult> findEventListItemsByStatus(EventStatus status, PageQuery pageQuery);

    Optional<EventDetailResult> findEventBannerById(EventId eventId);

    PageResult<EventAnnouncementResult> findAnnouncements(PageQuery pageQuery);
}
