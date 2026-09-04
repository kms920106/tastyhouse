package com.tastyhouse.adminapi.shop.adapter.in.web.request;

import com.tastyhouse.application.shop.port.in.ShopContentBoardHiddenChangeCommand;

import jakarta.validation.constraints.NotNull;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "가게 콘텐츠보드 숨김 처리 요청")
public record ShopContentBoardHideRequest(
    @NotNull(message = "숨김 여부는 필수입니다.")
    @Schema(description = "숨김 여부", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
    boolean hidden
) {

    public ShopContentBoardHiddenChangeCommand toCommand(Long contentBoardId) {
        return new ShopContentBoardHiddenChangeCommand(contentBoardId, hidden);
    }
}
