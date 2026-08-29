package com.tastyhouse.adminapi.shop.adapter.in.web.request;

import com.tastyhouse.adminapi.shop.application.port.in.ShopNoticeHideCommand;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "점주 공지 게시중단 요청")
public record ShopNoticeHideRequest(
    @NotBlank(message = "게시중단 사유는 필수입니다.")
    @Size(max = 200, message = "게시중단 사유는 최대 200자까지 입력할 수 있습니다.")
    @Schema(description = "게시중단 사유 (최대 200자)", example = "저작권 침해 소지가 있는 이미지가 포함되어 있습니다.",
        requiredMode = Schema.RequiredMode.REQUIRED)
    String reason
) {

    public ShopNoticeHideCommand toCommand(Long adminId, Long noticeId) {
        return new ShopNoticeHideCommand(adminId, noticeId, reason);
    }
}
