package com.tastyhouse.webapi.shop.adapter.in.web.request;

import java.util.Locale;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "가게 배달팁 조회 요청")
public record ShopDeliveryTipSearchRequest(
    @Schema(description = "배달 주소 ID. 주문금액과 함께 주면 확정 배달팁을 계산합니다. 없으면 범위만 반환합니다.", example = "12")
    Long deliveryAddressId,

    @Schema(description = "주문금액(원). 상품 할인 후·쿠폰/포인트 차감 전 금액이며 구간별 배달팁 판정 기준입니다.", example = "15000")
    Integer orderAmount,

    @Schema(description = "주문 방법(TABLE, RESERVATION, DELIVERY, TAKEOUT). 미지정 시 DELIVERY입니다.", example = "DELIVERY")
    String orderMethod
) {
    private static final String DEFAULT_ORDER_METHOD = "DELIVERY";

    public ShopDeliveryTipSearchRequest {
        orderMethod = orderMethod == null || orderMethod.isBlank()
            ? DEFAULT_ORDER_METHOD
            : orderMethod.strip().toUpperCase(Locale.ROOT);
    }
}
