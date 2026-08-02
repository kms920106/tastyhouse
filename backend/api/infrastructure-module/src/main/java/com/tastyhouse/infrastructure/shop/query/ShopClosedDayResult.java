package com.tastyhouse.infrastructure.shop.query;

import com.tastyhouse.domain.shop.model.ClosedDayType;

/**
 * 가게 정기휴무 한 건(회원 가게정보·점주 설정·관리 화면 공용).
 *
 * <p>같은 데이터를 도메인 서비스도 읽지만(등록 개수 제한 검증·영업 상태 판정), 그쪽은 도메인 모델
 * {@code ShopClosedDay}를 write 포트로 로드한다 — 목적과 반환 타입이 달라 중복이 아니다.
 */
public record ShopClosedDayResult(
    Long id,
    ClosedDayType closedDayType
) {
}
