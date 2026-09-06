package com.tastyhouse.webapi.shop.adapter.in.web.response;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.shop.port.out.ShopOrderMethodItemResult;

@Schema(description = "주문 방식 항목")
public record ShopOrderMethodItemResponse(
    @Schema(description = "주문 방식 코드", example = "DELIVERY",
        allowableValues = {"TABLE", "RESERVATION", "DELIVERY", "TAKEOUT"})
    String code,

    @Schema(description = "주문 방식 이름", example = "배달")
    String name,

    @Schema(description = "이 주문 방식으로 주문 가능한지 여부", example = "false")
    boolean orderable,

    @Schema(description = "불가 사유 코드. 주문 가능하면 null", example = "SUSPENDED")
    String unavailableReason,

    @Schema(description = "불가 사유 한글 문구. 주문 가능하면 null", example = "영업 임시중지 중입니다")
    String unavailableReasonName
) {
    public static ShopOrderMethodItemResponse from(ShopOrderMethodItemResult result) {
        return new ShopOrderMethodItemResponse(
            result.code(),
            result.name(),
            result.orderable(),
            result.unavailableReason(),
            result.unavailableReasonName()
        );
    }
}
