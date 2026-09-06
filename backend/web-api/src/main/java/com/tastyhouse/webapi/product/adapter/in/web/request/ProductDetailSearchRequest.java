package com.tastyhouse.webapi.product.adapter.in.web.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "메뉴 상세 조회 요청")
public record ProductDetailSearchRequest(
    @Schema(description = "가격을 해석할 주문유형. 미지정이면 DELIVERY로 조회합니다.",
        allowableValues = {"TABLE", "RESERVATION", "DELIVERY", "TAKEOUT"},
        example = "TAKEOUT")
    String orderMethod
) {
    private static final String DEFAULT_ORDER_METHOD = "DELIVERY";

    public ProductDetailSearchRequest {
        if (orderMethod == null || orderMethod.isBlank()) {
            orderMethod = DEFAULT_ORDER_METHOD;
        }
    }
}
