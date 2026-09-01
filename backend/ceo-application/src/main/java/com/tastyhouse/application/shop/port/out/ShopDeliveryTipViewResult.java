package com.tastyhouse.application.shop.port.out;

import java.util.List;

/**
 * 배달팁 설정 통합 조회 결과 — 구간·추가팁 유형·거리별 설정·지역별·시간별·공휴일 팁.
 *
 * <p><b>챕터 09</b>에서 신설. 배달팁은 파트마다 조회가 갈려 있어 유스케이스가 여러 번 조회해 합친다
 * (설정 헤더·구간·지역별·시간별·공휴일 금액). 표현 계약이 {@code from(Result)} 한 번으로 끝낼 수
 * 있도록 그 결과를 이 record로 묶는다.
 *
 * <p>{@code setting}이 {@code null}이면 배달팁을 한 번도 설정하지 않은 가게다 — 추가팁 유형 기본값
 * ({@code NONE})과 거리별 설정 비움 판정은 표현 규칙이므로 {@code ShopDeliveryTipSettingResponse}가
 * 수행한다.
 */
public record ShopDeliveryTipViewResult(
    ShopDeliveryTipSettingResult setting,
    List<ShopDeliveryTipTierResult> tiers,
    List<ShopDeliveryTipRegionResult> regions,
    List<ShopDeliveryTipScheduleResult> schedules,
    Integer holidayTipAmount
) {
}
