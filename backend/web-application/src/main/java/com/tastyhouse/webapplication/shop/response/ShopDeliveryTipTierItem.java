package com.tastyhouse.webapplication.shop.response;

import io.swagger.v3.oas.annotations.media.Schema;

/** 구간별 배달팁 한 행 — 팝업의 "주문금액별 배달팁" 표를 그린다. */
@Schema(description = "주문금액 구간별 배달팁")
public record ShopDeliveryTipTierItem(
    @Schema(description = "이 구간이 적용되는 최소 주문금액(원)", example = "15000")
    int minOrderAmount,

    @Schema(description = "이 구간의 배달팁(원)", example = "1000")
    int tipAmount
) {
    public static ShopDeliveryTipTierItem from(
        int minOrderAmount,
        int tipAmount
    ) {
        return new ShopDeliveryTipTierItem(
            minOrderAmount,
            tipAmount
        );
    }
}
