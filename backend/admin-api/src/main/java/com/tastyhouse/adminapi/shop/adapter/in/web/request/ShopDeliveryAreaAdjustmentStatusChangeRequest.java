package com.tastyhouse.adminapi.shop.adapter.in.web.request;

import com.tastyhouse.adminapplication.shop.port.in.ShopDeliveryAreaAdjustmentStatusChangeCommand;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "배달지역 조정 신청 상태 변경 요청")
public record ShopDeliveryAreaAdjustmentStatusChangeRequest(
    @NotBlank(message = "변경할 상태는 필수입니다.")
    @Schema(
        description = "변경할 상태. IN_PROGRESS(가맹본부 전달·조정 개시) 또는 COMPLETED(조정 성립)",
        example = "IN_PROGRESS",
        allowableValues = {"IN_PROGRESS", "COMPLETED"},
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    String status
) {

    public ShopDeliveryAreaAdjustmentStatusChangeCommand toCommand(Long requestId) {
        return new ShopDeliveryAreaAdjustmentStatusChangeCommand(requestId, status);
    }
}
