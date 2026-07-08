package com.tastyhouse.webapi.shop.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "매장 주문 방식 목록 응답")
public record ShopOrderMethodResponse(
    @Schema(description = "주문 방식 목록")
    List<OrderMethodItem> orderMethods
) {
    public static ShopOrderMethodResponse from(
        List<OrderMethodItem> orderMethods
    ) {
        return new ShopOrderMethodResponse(
            orderMethods
        );
    }

    @Schema(description = "주문 방식 항목")
    public record OrderMethodItem(
        @Schema(description = "주문 방식 코드", example = "DELIVERY")
        String code,
        @Schema(description = "주문 방식 이름", example = "배달")
        String name
    ) {
        public static OrderMethodItem from(
            String code,
            String name
        ) {
            return new OrderMethodItem(
                code,
                name
            );
        }
    }
}
