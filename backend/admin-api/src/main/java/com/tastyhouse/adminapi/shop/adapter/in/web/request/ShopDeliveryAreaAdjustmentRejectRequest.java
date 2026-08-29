package com.tastyhouse.adminapi.shop.adapter.in.web.request;

import com.tastyhouse.adminapi.shop.application.port.in.ShopDeliveryAreaAdjustmentRejectCommand;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "배달지역 조정 신청 반려 요청")
public record ShopDeliveryAreaAdjustmentRejectRequest(
    @NotBlank(message = "반려 사유는 필수입니다.")
    @Size(max = 500, message = "반려 사유는 500자를 초과할 수 없습니다.")
    @Schema(description = "반려 사유", example = "첨부된 동의서가 식별되지 않습니다.", requiredMode = Schema.RequiredMode.REQUIRED)
    String reason
) {

    public ShopDeliveryAreaAdjustmentRejectCommand toCommand(Long requestId) {
        return new ShopDeliveryAreaAdjustmentRejectCommand(requestId, reason);
    }
}
