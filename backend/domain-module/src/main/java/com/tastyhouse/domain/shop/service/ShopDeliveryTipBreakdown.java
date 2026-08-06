package com.tastyhouse.domain.shop.service;

/**
 * 배달팁 산출 결과 — 항목별 금액과 총액.
 *
 * <p>총액만이 아니라 항목별로 돌려주는 이유는 (1) 주문서 화면이 "기본 2,000 + 거리 1,500"처럼 근거를
 * 보여줘야 하고, (2) 금액 불일치 CS 때 어느 항목이 갈렸는지 추적해야 하기 때문이다.
 */
public record ShopDeliveryTipBreakdown(
    int baseTipAmount,
    int distanceTipAmount,
    int regionTipAmount,
    int scheduleTipAmount,
    int holidayTipAmount,
    int totalTipAmount
) {

    /** 배달팁이 전혀 부과되지 않는 결과(배달 외 주문 방법 등). */
    public static ShopDeliveryTipBreakdown none() {
        return new ShopDeliveryTipBreakdown(0, 0, 0, 0, 0, 0);
    }

    public static ShopDeliveryTipBreakdown of(
        int baseTipAmount,
        int distanceTipAmount,
        int regionTipAmount,
        int scheduleTipAmount,
        int holidayTipAmount
    ) {
        return new ShopDeliveryTipBreakdown(
            baseTipAmount,
            distanceTipAmount,
            regionTipAmount,
            scheduleTipAmount,
            holidayTipAmount,
            baseTipAmount + distanceTipAmount + regionTipAmount + scheduleTipAmount + holidayTipAmount
        );
    }
}
