package com.tastyhouse.adminapi.shop.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "가게 음식종류 지정 요청")
public record ShopFoodTypeAssignRequest(
    @NotNull(message = "음식종류 카테고리 ID는 필수입니다.")
    @Schema(description = "음식종류 카테고리 ID", example = "5", requiredMode = Schema.RequiredMode.REQUIRED)
    Long foodTypeCategoryId
) {
}
