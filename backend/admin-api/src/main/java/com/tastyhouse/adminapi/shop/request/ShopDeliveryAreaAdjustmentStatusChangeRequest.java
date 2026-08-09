package com.tastyhouse.adminapi.shop.request;

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
}
