package com.tastyhouse.adminapi.shop.adapter.in.web.request;

import com.tastyhouse.application.shop.port.in.ShopMenuCollectionImageRejectCommand;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "메뉴모음컷 반려 요청")
public record ShopMenuCollectionImageRejectRequest(
    @NotBlank(message = "반려 사유는 필수입니다.")
    @Size(max = 500, message = "반려 사유는 500자 이하여야 합니다.")
    @Schema(description = "반려 사유", example = "메뉴가 잘 보이지 않습니다.",
        requiredMode = Schema.RequiredMode.REQUIRED)
    String rejectReason
) {
    public ShopMenuCollectionImageRejectCommand toCommand(Long imageId) {
        return new ShopMenuCollectionImageRejectCommand(imageId, rejectReason);
    }
}
