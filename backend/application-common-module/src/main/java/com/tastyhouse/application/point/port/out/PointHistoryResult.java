package com.tastyhouse.application.point.port.out;

import java.time.LocalDateTime;

import com.tastyhouse.domain.point.model.PointType;

/**
 * 포인트 변동 이력 조회 결과.
 *
 * <p>web(내 포인트 내역)·admin(회원 포인트 이력) 양쪽이 같은 필드 셋을 소비하므로 하나로 둔다.
 * 표현 목적 read의 산출물이므로 domain(write 포트)이 아니라 이 query 패키지가 소유한다.
 */
public record PointHistoryResult(
    PointType pointType,
    Integer pointAmount,
    String reason,
    LocalDateTime createdAt
) {
}
