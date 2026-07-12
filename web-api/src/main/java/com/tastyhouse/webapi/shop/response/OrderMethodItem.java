package com.tastyhouse.webapi.shop.response;

import io.swagger.v3.oas.annotations.media.Schema;

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
