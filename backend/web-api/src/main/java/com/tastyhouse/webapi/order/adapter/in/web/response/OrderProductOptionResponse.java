package com.tastyhouse.webapi.order.adapter.in.web.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "주문 상품 옵션 응답")
public record OrderProductOptionResponse(
    @Schema(description = "주문 상품 옵션 ID", example = "1")
    Long id,

    @Schema(description = "옵션 그룹명", example = "맵기 선택")
    String optionGroupName,

    @Schema(description = "옵션명", example = "매운맛")
    String optionName,

    @Schema(description = "추가 금액. 보증금은 여기 포함되지 않는 별도 항목이다.", example = "1000")
    Integer additionalPrice,

    @Schema(description = "주문 시점 옵션그룹 유형 스냅샷", example = "NORMAL",
        allowableValues = {"NORMAL", "CUP_DEPOSIT"})
    String optionGroupType,

    @Schema(description = "주문 시점 일회용컵 제공 개수 스냅샷", example = "1")
    Integer cupCount,

    @Schema(description = "주문 시점 일회용컵 보증금 금액 스냅샷. 비과세이며 반납 시 환급 대상이다.",
        example = "300")
    Integer depositAmount
) {
    public static OrderProductOptionResponse from(
        Long id,
        String optionGroupName,
        String optionName,
        Integer additionalPrice,
        String optionGroupType,
        Integer cupCount,
        Integer depositAmount
    ) {
        return new OrderProductOptionResponse(
            id,
            optionGroupName,
            optionName,
            additionalPrice,
            optionGroupType,
            cupCount,
            depositAmount
        );
    }
}
