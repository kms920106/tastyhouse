package com.tastyhouse.ceoapi.shop.adapter.in.web.response;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.shop.port.out.ShopOrderMethodResult;

@Schema(description = "내 가게 주문유형 배정 항목")
public record ShopOrderMethodItemResponse(
    @Schema(description = "배정 ID", example = "12")
    Long id,

    @Schema(description = "주문유형", example = "DELIVERY",
        allowableValues = {"TABLE", "RESERVATION", "DELIVERY", "TAKEOUT"})
    String orderMethod,

    @Schema(description = "주문유형 한글명", example = "배달")
    String orderMethodName
) {

    public static ShopOrderMethodItemResponse from(ShopOrderMethodResult result) {
        return new ShopOrderMethodItemResponse(
            result.id(),
            result.orderMethod().name(),
            result.orderMethod().getDisplayName()
        );
    }
}
