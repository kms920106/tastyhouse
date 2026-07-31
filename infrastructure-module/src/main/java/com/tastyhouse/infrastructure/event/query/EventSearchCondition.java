package com.tastyhouse.infrastructure.event.query;

import com.tastyhouse.domain.event.domain.model.EventStatus;

/**
 * 이벤트 관리 목록 검색 조건(admin).
 *
 * <p>표현 목적 read의 입력이므로 domain(write 포트)이 아니라 이 query 패키지가 소유한다. 두 필드 모두
 * null이면 조건을 적용하지 않는다(전체 조회).
 */
public record EventSearchCondition(
    String name,
    EventStatus status
) {

    public static EventSearchCondition of(String name, EventStatus status) {
        return new EventSearchCondition(name, status);
    }
}
