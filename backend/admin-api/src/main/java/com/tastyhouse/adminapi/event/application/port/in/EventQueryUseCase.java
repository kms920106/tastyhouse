package com.tastyhouse.adminapi.event.application.port.in;

import java.util.List;

import com.tastyhouse.adminapi.event.adapter.in.web.response.EventAnnouncementResponse;
import com.tastyhouse.adminapi.event.adapter.in.web.response.EventDetailResponse;
import com.tastyhouse.adminapi.event.adapter.in.web.response.EventListItemResponse;
import com.tastyhouse.adminapi.event.adapter.in.web.response.EventWinnerResponse;
import com.tastyhouse.apicommon.common.PaginationResponse;

/**
 * 이벤트 조회 인바운드 포트.
 *
 * <p>컨트롤러는 이 인터페이스만 주입하고 구현({@code EventQueryService})을 알지 않는다. 도입 근거는
 * 다형성이 아니라 컴파일 게이트와 경계 계약의 문서화다(backend/CLAUDE.md 인바운드 포트 절).
 */
public interface EventQueryUseCase {

    PaginationResponse<EventListItemResponse> getEvents(String name, String status, int page, int size);

    EventDetailResponse getEvent(Long id);

    EventAnnouncementResponse getAnnouncement(Long id);

    List<EventWinnerResponse> getWinners(Long id);
}
