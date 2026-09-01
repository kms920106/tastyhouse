package com.tastyhouse.ceoapi.shop.adapter.in.web.response;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.shop.port.out.ShopDeliveryTipTierResult;

@Schema(description = "구간별 기본 배달팁 한 구간")
public record ShopDeliveryTipTierItemResponse(
    @Schema(description = "구간 ID", example = "10")
    long id,

    @Schema(description = "구간 순서(0부터, 주문금액 오름차순)", example = "0")
    int tierOrder,

    @Schema(description = "이 구간이 적용되는 최소주문금액(원)", example = "5000")
    int minOrderAmount,

    @Schema(description = "이 구간의 배달팁(원)", example = "2000")
    int tipAmount
) {
    public static ShopDeliveryTipTierItemResponse from(ShopDeliveryTipTierResult result) {
        return new ShopDeliveryTipTierItemResponse(
            result.id(),
            result.tierOrder(),
            result.minOrderAmount(),
            result.tipAmount()
        );
    }
}
