package com.tastyhouse.ceoapi.shop.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "내 가게 노출 상태 변경 요청")
public record ShopStatusUpdateRequest(
    @NotBlank(message = "가게 상태는 필수입니다.")
    @Schema(description = "가게 노출 상태", example = "OPEN", allowableValues = {"OPEN", "HIDDEN"},
        requiredMode = Schema.RequiredMode.REQUIRED)
    String status
) {
}
