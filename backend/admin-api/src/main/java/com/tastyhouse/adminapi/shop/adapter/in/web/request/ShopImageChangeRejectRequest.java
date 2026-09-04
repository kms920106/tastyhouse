package com.tastyhouse.adminapi.shop.adapter.in.web.request;

import com.tastyhouse.application.shop.port.in.ShopImageChangeRejectCommand;

import jakarta.validation.constraints.NotBlank;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "가게 이미지 변경 요청 반려 요청")
public record ShopImageChangeRejectRequest(
    @NotBlank(message = "반려 사유는 필수입니다.")
    @Schema(description = "반려 사유", example = "이미지 해상도가 기준에 미달합니다.", requiredMode = Schema.RequiredMode.REQUIRED)
    String reason
) {

    public ShopImageChangeRejectCommand toCommand(Long requestId) {
        return new ShopImageChangeRejectCommand(requestId, reason);
    }
}
