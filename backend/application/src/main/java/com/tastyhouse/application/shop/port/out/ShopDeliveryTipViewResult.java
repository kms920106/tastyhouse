package com.tastyhouse.application.shop.port.out;

import java.util.List;

/**
 * 배달팁 팝업·재견적 결과 — 확정 모드와 범위 모드를 한 계약으로 표현한다.
 *
 * <p><b>챕터 10</b>에서 신설. <b>배달팁은 주문 총액의 유일한 가산 항목</b>이라 이 계약의 조립은 전부
 * 서비스에 남는다 — 주소 소유권 검증, 직선거리 산출, 공휴일 판정, 도메인 계산기 호출, 확정/범위 모드
 * 분기, 항목별 근거 문구, 거리별 설정의 노출 여부 판정이 모두 계산이며 web-api는 그중 무엇도 할 수
 * 없다. 이 record는 그 결과를 담기만 하고, Response의 {@code from}은 필드를 복사한다.
 *
 * <p>{@code deliveryTip}이 {@code null}이면 <b>범위 모드</b>다(확정 입력이 하나라도 없거나 비로그인).
 * 그때 {@code breakdown}은 빈 목록이며, 프론트는 {@code minDeliveryTip}~{@code maxDeliveryTip}을
 * 보여준다.
 *
 * <p>{@code extraTipType}은 미설정 가게의 기본값({@code NONE})까지 서비스가 정해 강등한 String이고,
 * {@code distance}는 거리별을 쓰지 않는 가게에서 {@code null}이다(거리별↔지역별 상호 배타). 두 판정
 * 모두 도메인 enum 비교라 web-api가 수행할 수 없다.
 *
 * <p>{@code tiers}·{@code regions}는 표현 조립이 없어 공유 읽기 계약을 그대로 담는다.
 */
public record ShopDeliveryTipViewResult(
    Integer deliveryTip,
    int minDeliveryTip,
    int maxDeliveryTip,
    List<ShopDeliveryTipBreakdownItemResult> breakdown,
    List<ShopDeliveryTipTierResult> tiers,
    String extraTipType,
    ShopDeliveryTipSettingResult distance,
    List<ShopDeliveryTipRegionResult> regions,
    List<ShopDeliveryTipScheduleItemResult> schedules,
    int holidayTipAmount
) {
}
