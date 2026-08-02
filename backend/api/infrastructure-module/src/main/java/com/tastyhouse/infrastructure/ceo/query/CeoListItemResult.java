package com.tastyhouse.infrastructure.ceo.query;

import com.querydsl.core.annotations.QueryProjection;

import com.tastyhouse.domain.ceo.model.CeoStatus;

/**
 * 점주 목록 항목 조회 결과.
 *
 * <p>비-admin 형제가 없어 {@code Management} 한정어 없이 순수명을 쓴다. 식별자는 HTTP 경계까지
 * 그대로 전달되는 표현용 값이므로 도메인 VO({@code CeoId})가 아니라 {@code Long}으로 투영한다.
 */
public record CeoListItemResult(
    Long id,
    String name,
    String businessRegistrationNumber,
    CeoStatus status
) {

    @QueryProjection
    public CeoListItemResult {
    }
}
