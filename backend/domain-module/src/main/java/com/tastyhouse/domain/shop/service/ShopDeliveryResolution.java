package com.tastyhouse.domain.shop.service;

/**
 * 배달 판정 결과 — 가게~배달지 거리와 배달팁 내역.
 *
 * <p>둘은 같은 판정(배달가능지역 확인 + 거리 산출 + 팁 계산)에서 함께 나오고 주문 접수에서 함께
 * 소비되므로, 따로 돌려주면 호출부가 두 값을 짝지어 들고 다녀야 한다.
 *
 * @param distanceMeters 가게~배달지 직선거리(m). 주문 목적지 스냅샷에 박제된다
 * @param tipBreakdown   항목별 배달팁 내역
 */
public record ShopDeliveryResolution(
    int distanceMeters,
    ShopDeliveryTipBreakdown tipBreakdown
) {
}
