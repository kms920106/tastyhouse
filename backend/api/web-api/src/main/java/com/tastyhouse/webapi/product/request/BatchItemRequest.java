package com.tastyhouse.webapi.product.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "조회 항목 (상품ID + 옵션ID)")
public record BatchItemRequest(
    @Schema(description = "상품 ID", example = "1")
    @NotNull(message = "상품 ID는 필수입니다.")
    Long productId,

    @Schema(description = "옵션 ID. 옵션이 없는 항목이면 null 가능", example = "1")
    Long optionId
) {
}
