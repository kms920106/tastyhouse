package com.tastyhouse.ceoapi.shop.adapter.in.web.response;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.shop.port.out.ShopOrderAvailabilityViewResult;
import com.tastyhouse.domain.shop.model.OrderUnavailableReason;

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
        ShopOrderAvailabilityViewResult.OrderMethodAvailability availability
    ) {
        OrderUnavailableReason reason = availability.unavailableReason();
        return new ShopOrderMethodAvailabilityResponse(
            availability.orderMethod().name(),
            availability.orderMethod().getDisplayName(),
            availability.orderable(),
            reason == null ? null : reason.name(),
            reason == null ? null : reason.getDisplayName()
        );
    }
}
