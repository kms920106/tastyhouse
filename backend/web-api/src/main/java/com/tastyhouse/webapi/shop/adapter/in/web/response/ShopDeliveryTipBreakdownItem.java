package com.tastyhouse.webapi.shop.adapter.in.web.response;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.shop.port.out.ShopDeliveryTipBreakdownItemResult;

@Schema(description = "배달팁 산출 근거 항목")
public record ShopDeliveryTipBreakdownItem(
    @Schema(description = "항목 설명", example = "주문금액 15,000원 이상")
    String label,

    @Schema(description = "항목 금액(원)", example = "1000")
    int amount
) {
    public static ShopDeliveryTipBreakdownItem from(ShopDeliveryTipBreakdownItemResult result) {
        return new ShopDeliveryTipBreakdownItem(
            result.label(),
            result.amount()
        );
    }
}
