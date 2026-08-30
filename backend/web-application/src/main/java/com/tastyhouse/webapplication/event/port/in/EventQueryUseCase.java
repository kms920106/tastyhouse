package com.tastyhouse.webapplication.event.port.in;

import com.tastyhouse.apicommon.common.PaginationResponse;
import com.tastyhouse.webapplication.event.response.EventAnnouncementListItemResponse;
import com.tastyhouse.webapplication.event.response.EventDetailResponse;
import com.tastyhouse.webapplication.event.response.EventListItemResponse;

/**
 * 이벤트 조회 인바운드 포트.
 *
 * <p>컨트롤러는 이 인터페이스만 주입하고 구현({@code EventQueryService})을 알지 않는다. 도입 근거는
 * 다형성이 아니라 컴파일 게이트와 경계 계약의 문서화다(backend/CLAUDE.md 인바운드 포트 절).
 */
public interface EventQueryUseCase {

    PaginationResponse<EventListItemResponse> getEventList(String status, int page, int size);

    EventDetailResponse getEventDetail(Long eventId);

    PaginationResponse<EventAnnouncementListItemResponse> getEventAnnouncementList(int page, int size);
}
