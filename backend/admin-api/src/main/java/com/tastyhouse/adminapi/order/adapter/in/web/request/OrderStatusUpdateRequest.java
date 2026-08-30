package com.tastyhouse.adminapi.order.adapter.in.web.request;

import com.tastyhouse.adminapplication.order.port.in.OrderStatusChangeCommand;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "주문 상태 변경 요청")
public record OrderStatusUpdateRequest(
    @NotBlank(message = "상태는 필수입니다.")
    @Schema(description = "변경할 주문 상태", example = "CONFIRMED", allowableValues = {"PENDING", "CONFIRMED", "PREPARING", "COMPLETED", "CANCELLED"}, requiredMode = Schema.RequiredMode.REQUIRED)
    String status
) {

    public OrderStatusChangeCommand toCommand(Long orderId) {
        return new OrderStatusChangeCommand(orderId, status());
    }
}
