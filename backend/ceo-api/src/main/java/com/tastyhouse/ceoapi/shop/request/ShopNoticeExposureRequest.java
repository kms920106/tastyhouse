package com.tastyhouse.ceoapi.shop.request;

import jakarta.validation.constraints.NotNull;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "점주 공지 앱 노출 토글 요청")
public record ShopNoticeExposureRequest(
    @NotNull(message = "노출 여부는 필수입니다.")
    @Schema(description = "앱 노출 여부 (true=앱에 반영, false=앱에서 내리기)", example = "true",
        requiredMode = Schema.RequiredMode.REQUIRED)
    Boolean exposed
) {
}
