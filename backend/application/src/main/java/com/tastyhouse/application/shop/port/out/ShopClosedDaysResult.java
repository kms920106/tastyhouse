package com.tastyhouse.application.shop.port.out;

import java.util.List;

/**
 * 가게 휴무 통합 조회 결과 — 공휴일 휴무 여부·정기 휴무·임시 휴무.
 *
 * <p><b>챕터 09</b>에서 신설. 공휴일 휴무 여부는 소유권 검증이 반환한 {@code Shop} <b>애그리거트</b>에서
 * 나오고 두 휴무 목록은 서로 다른 읽기 포트에서 나온다. 표현 계약은 도메인 모델을 알 수 없으므로
 * ({@code apiModuleShouldBeDomainModelFree}) application이 세 출처를 이 record로 합쳐 넘긴다.
 */
public record ShopClosedDaysResult(
    boolean closedOnPublicHolidays,
    List<ShopClosedDayResult> regularClosedDays,
    List<ShopTemporaryClosureResult> temporaryClosures
) {
}
