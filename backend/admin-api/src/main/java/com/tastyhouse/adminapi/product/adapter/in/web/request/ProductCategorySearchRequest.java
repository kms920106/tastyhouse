package com.tastyhouse.adminapi.product.adapter.in.web.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "상품 카테고리 검색 요청")
public record ProductCategorySearchRequest(
    @NotNull(message = "매장 ID는 필수입니다.")
    @Schema(description = "매장 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    Long shopId
) {
}
