package com.tastyhouse.infrastructure.ceo.query;

import java.time.LocalDateTime;

import com.tastyhouse.domain.ceo.model.CeoLoginFailureReason;
import com.tastyhouse.domain.ceo.model.CeoLoginResult;

/**
 * 점주 로그인 이력 목록 항목 조회 결과.
 *
 * <p>반드시 {@code public}이어야 한다 — package-private이면 QueryDSL {@code Projections.constructor}가
 * {@code Class#getConstructors()}(public 생성자만 반환)에서 생성자를 찾지 못해 <b>컴파일은 통과하고 그 쿼리
 * 실행 시에만 500</b>이 난다. {@code QueryResultRecordVisibilityTest}가 이를 가드한다.
 *
 * <p>{@code ceoId}는 투영하지 않는다 — 토큰의 점주 것만 조회하므로 응답에 되돌려 줄 의미가 없다.
 */
public record CeoLoginHistoryResult(
    Long id,
    CeoLoginResult result,
    CeoLoginFailureReason failureReason,
    String ipAddress,
    String userAgent,
    LocalDateTime loggedInAt
) {
}
