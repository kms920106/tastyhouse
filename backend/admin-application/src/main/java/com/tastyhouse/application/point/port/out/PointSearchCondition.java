package com.tastyhouse.application.point.port.out;

import com.tastyhouse.domain.point.model.PointType;

/**
 * 포인트 이력 검색 조건.
 *
 * <p>표현 목적 read의 입력이므로 domain(write 포트)이 아니라 이 query 패키지가 소유한다. 소비 모듈
 * (admin-api)의 {@code PointQueryService}가 String→enum 승격을 마친 값으로 조립해 전달한다.
 * {@code memberId}는 query 계층 규약대로 raw {@code Long}으로 받는다.
 * {@code pointType}이 null이면 전체 유형을 조회한다.
 */
public record PointSearchCondition(
    Long memberId,
    PointType pointType
) {

    public static PointSearchCondition of(Long memberId, PointType pointType) {
        return new PointSearchCondition(memberId, pointType);
    }
}
