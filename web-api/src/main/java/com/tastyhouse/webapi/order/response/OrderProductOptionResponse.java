package com.tastyhouse.webapi.order.response;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.core.domain.order.application.dto.result.OrderProductOptionResult;

@Schema(description = "주문 상품 옵션 응답")
public record OrderProductOptionResponse(
    @Schema(description = "주문 상품 옵션 ID", example = "1")
    Long id,

    @Schema(description = "옵션 그룹명", example = "맵기 선택")
    String optionGroupName,

    @Schema(description = "옵션명", example = "매운맛")
    String optionName,

    @Schema(description = "추가 금액", example = "1000")
    Integer additionalPrice
) {
    public static OrderProductOptionResponse from(OrderProductOptionResult option) {
        return new OrderProductOptionResponse(
            option.orderProductOptionId().value(),
            option.optionGroupName(),
            option.optionName(),
            option.additionalPrice()
        );
    }
}
