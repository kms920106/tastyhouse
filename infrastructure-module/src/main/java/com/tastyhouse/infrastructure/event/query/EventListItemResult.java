package com.tastyhouse.infrastructure.event.query;

import java.time.LocalDateTime;

import com.querydsl.core.annotations.QueryProjection;

/**
 * 이벤트 목록 조회 결과(web 노출 목록).
 *
 * <p>표현 목적 read의 산출물이므로 domain(write 포트)이 아니라 이 query 패키지가 소유한다. admin
 * 관리 목록용 형제({@link EventManagementListItemResult})와 같은 패키지에 공존하므로, 관리 화면 쪽이
 * {@code Management} 한정어를 유지해 이름 충돌을 피한다.
 */
public record EventListItemResult(
    Long eventId,
    String name,
    String thumbnailFilePath,
    LocalDateTime startAt,
    LocalDateTime endAt
) {

    @QueryProjection
    public EventListItemResult {
    }
}
