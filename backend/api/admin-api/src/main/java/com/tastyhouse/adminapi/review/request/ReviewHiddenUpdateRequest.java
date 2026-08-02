package com.tastyhouse.adminapi.review.request;

import jakarta.validation.constraints.NotNull;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "숨김 여부 변경 요청")
public record ReviewHiddenUpdateRequest(
    @NotNull(message = "숨김 여부는 필수입니다.")
    @Schema(description = "숨김 여부", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
    Boolean hidden
) {
}
