package com.tastyhouse.adminapi.shop.adapter.in.web.request;

import com.tastyhouse.application.shop.port.in.ShopOrderNoticeHideCommand;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "주문안내 게시중단 요청")
public record ShopOrderNoticeHideRequest(
    @NotBlank(message = "게시중단 사유는 필수입니다.")
    @Size(max = 500, message = "게시중단 사유는 최대 500자까지 입력할 수 있습니다.")
    @Schema(description = "게시중단 사유 (최대 500자)", example = "배민 외 결제 유도 문구가 포함되어 있습니다.",
        requiredMode = Schema.RequiredMode.REQUIRED)
    String reason
) {
    public ShopOrderNoticeHideCommand toCommand(Long shopId) {
        return new ShopOrderNoticeHideCommand(shopId, reason);
    }
}
