package com.tastyhouse.adminapi.shop.adapter.in.web.request;

import com.tastyhouse.adminapi.shop.application.port.in.ShopBannerImageCreateCommand;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "가게 배너 이미지 등록 요청")
public record ShopBannerImageSaveRequest(
    @NotNull(message = "이미지 파일 ID는 필수입니다.")
    @Schema(description = "배너 이미지 파일 ID", example = "30", requiredMode = Schema.RequiredMode.REQUIRED)
    Long imageFileId,

    @Schema(description = "정렬 순서", example = "1")
    Integer sort
) {

    public ShopBannerImageCreateCommand toCommand(Long shopId) {
        return new ShopBannerImageCreateCommand(shopId, imageFileId, sort);
    }
}
