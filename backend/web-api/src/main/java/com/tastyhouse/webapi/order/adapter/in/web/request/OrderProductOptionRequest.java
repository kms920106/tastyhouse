package com.tastyhouse.webapi.order.adapter.in.web.request;

import com.tastyhouse.application.order.port.in.OrderLineOptionCommand;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "주문 상품 옵션 요청")
public record OrderProductOptionRequest(
    @NotNull(message = "옵션 그룹 ID는 필수입니다")
    @Schema(description = "옵션 그룹 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    Long groupId,

    @NotNull(message = "옵션 ID는 필수입니다")
    @Schema(description = "옵션 ID", example = "3", requiredMode = Schema.RequiredMode.REQUIRED)
    Long optionId
) {

    public OrderLineOptionCommand toCommand() {
        return OrderLineOptionCommand.of(groupId, optionId);
    }
}
