package com.tastyhouse.infrastructure.event.query;

import java.time.LocalDateTime;

import com.querydsl.core.annotations.QueryProjection;

import com.tastyhouse.domain.event.domain.model.EventStatus;

/**
 * 이벤트 관리 목록 조회 결과(admin).
 *
 * <p>표현 목적 read의 산출물이므로 domain(write 포트)이 아니라 이 query 패키지가 소유한다. web
 * 노출 목록용 형제({@link EventListItemResult})와 같은 패키지에 공존하므로, 두 result의 필드 셋이 다른
 * 만큼 통합하지 않고 관리 화면 용도를 나타내는 {@code Management} 한정어로 구별한다.
 *
 * <p>조인으로 얻은 저장 경로는 DAO가 {@code FileUrlResolver}로 표시용 URL까지 변환해 담는다.
 */
public record EventManagementListItemResult(
    Long id,
    String name,
    EventStatus status,
    Long thumbnailImageFileId,
    String thumbnailFileName,
    String thumbnailUrl,
    LocalDateTime startAt,
    LocalDateTime endAt
) {

    @QueryProjection
    public EventManagementListItemResult {
    }
}
