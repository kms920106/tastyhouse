package com.tastyhouse.ceoapi.shop.adapter.in.web.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import com.tastyhouse.application.shop.port.in.ShopStatusUpdateCommand;

@Schema(description = "내 가게 노출 상태 변경 요청")
public record ShopStatusUpdateRequest(
    @NotBlank(message = "가게 상태는 필수입니다.")
    @Schema(description = "가게 노출 상태", example = "OPEN", allowableValues = {"OPEN", "HIDDEN"},
        requiredMode = Schema.RequiredMode.REQUIRED)
    String status
) {

    public ShopStatusUpdateCommand toCommand(Long ceoId, Long shopId) {
        return new ShopStatusUpdateCommand(ceoId, shopId, status());
    }
}
