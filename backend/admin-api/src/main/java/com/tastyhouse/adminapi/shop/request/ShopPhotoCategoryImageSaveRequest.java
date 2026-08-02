package com.tastyhouse.adminapi.shop.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "가게 포토 카테고리 이미지 등록/수정 요청")
public record ShopPhotoCategoryImageSaveRequest(
    @NotNull(message = "이미지 파일 ID는 필수입니다.")
    @Schema(description = "이미지 파일 ID", example = "31", requiredMode = Schema.RequiredMode.REQUIRED)
    Long imageFileId,

    @NotNull(message = "정렬 순서는 필수입니다.")
    @Schema(description = "정렬 순서", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    Integer sort,

    @NotNull(message = "노출 여부는 필수입니다.")
    @Schema(description = "노출 여부", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
    Boolean visible
) {
}
