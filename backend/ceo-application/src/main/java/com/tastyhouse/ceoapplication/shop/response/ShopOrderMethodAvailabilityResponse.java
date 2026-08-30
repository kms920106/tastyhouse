package com.tastyhouse.ceoapplication.shop.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "주문유형별 주문가능 상태")
public record ShopOrderMethodAvailabilityResponse(
    @Schema(description = "주문유형", example = "DELIVERY",
        allowableValues = {"TABLE", "RESERVATION", "DELIVERY", "TAKEOUT"})
    String orderMethod,

    @Schema(description = "주문유형 한글명", example = "배달")
    String orderMethodName,

    @Schema(description = "이 주문유형으로 주문 가능한지 여부", example = "false")
    boolean orderable,

    @Schema(description = "불가 사유 코드. 주문 가능하면 null", example = "SUSPENDED")
    String unavailableReason,

    @Schema(description = "불가 사유 한글 문구. 주문 가능하면 null", example = "영업 임시중지 중입니다")
    String unavailableReasonName
) {

    public static ShopOrderMethodAvailabilityResponse from(
        String orderMethod,
        String orderMethodName,
        boolean orderable,
        String unavailableReason,
        String unavailableReasonName
    ) {
        return new ShopOrderMethodAvailabilityResponse(
            orderMethod,
            orderMethodName,
            orderable,
            unavailableReason,
            unavailableReasonName
        );
    }
}
