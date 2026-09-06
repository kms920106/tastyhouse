package com.tastyhouse.webapi.shop.adapter.in.web.request;

import java.util.Locale;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "예약 가능 수령시간 슬롯 조회 요청")
public record ScheduledOrderSlotSearchRequest(
    @NotBlank(message = "주문 방법은 필수입니다.")
    @Schema(
        description = "주문 방법. 예약주문은 DELIVERY·TAKEOUT만 지원하며, 그 외에는 available=false로 응답합니다.",
        example = "DELIVERY",
        allowableValues = {"TABLE", "RESERVATION", "DELIVERY", "TAKEOUT"},
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    String orderMethod
) {
    public ScheduledOrderSlotSearchRequest {
        orderMethod = orderMethod == null ? null : orderMethod.strip().toUpperCase(Locale.ROOT);
    }
}
