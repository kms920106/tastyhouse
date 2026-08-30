package com.tastyhouse.adminapplication.shop.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "가게 주문수단 응답")
public record ShopOrderMethodItemResponse(
    @Schema(description = "지정 ID", example = "1")
    Long id,

    @Schema(description = "주문수단", example = "TABLE")
    String orderMethod,

    @Schema(description = "주문수단 표시명", example = "테이블 오더")
    String displayName
) {
    public static ShopOrderMethodItemResponse from(
        Long id,
        String orderMethod,
        String displayName
    ) {
        return new ShopOrderMethodItemResponse(
            id,
            orderMethod,
            displayName
        );
    }
}
