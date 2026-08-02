package com.tastyhouse.infrastructure.point.query;

import com.querydsl.core.annotations.QueryProjection;

/**
 * 회원 포인트 잔액 조회 결과.
 *
 * <p>web(보유/사용 가능 포인트)·admin(잔액 관리 화면) 양쪽이 같은 필드 셋을 소비한다. 소비 모듈이
 * 실제로 쓰는 필드만 담으므로 회원 식별자는 조회 입력으로 이미 알고 있어 포함하지 않는다
 * (과거 {@code PointResult}가 갖고 있던 {@code memberId}는 admin 응답 조립 시 파라미터를 그대로
 * 되돌려주는 용도였으므로 제거했다).
 */
public record PointBalanceResult(
    Integer availablePoints,
    Integer expiredThisMonth
) {

    @QueryProjection
    public PointBalanceResult {
    }
}
