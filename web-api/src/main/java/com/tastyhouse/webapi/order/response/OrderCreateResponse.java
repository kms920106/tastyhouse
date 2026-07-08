package com.tastyhouse.webapi.order.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "주문 생성 응답")
public record OrderCreateResponse(
    @Schema(description = "생성된 주문 ID", example = "1")
    Long id
) {
    public static OrderCreateResponse from(
        Long id
    ) {
        return new OrderCreateResponse(
            id
        );
    }
}
