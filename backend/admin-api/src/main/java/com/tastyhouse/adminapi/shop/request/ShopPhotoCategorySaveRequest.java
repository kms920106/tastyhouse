package com.tastyhouse.adminapi.shop.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "가게 포토 카테고리 등록/수정 요청")
public record ShopPhotoCategorySaveRequest(
    @NotBlank(message = "카테고리명은 필수입니다.")
    @Schema(description = "포토 카테고리명", example = "가게 외관", requiredMode = Schema.RequiredMode.REQUIRED)
    String name
) {
}
