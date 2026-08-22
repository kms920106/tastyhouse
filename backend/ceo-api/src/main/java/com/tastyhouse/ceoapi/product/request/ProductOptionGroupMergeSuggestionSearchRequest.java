package com.tastyhouse.ceoapi.product.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "옵션그룹 합치기 추천 목록 조회 요청")
public record ProductOptionGroupMergeSuggestionSearchRequest(
    @NotNull(message = "가게 ID는 필수입니다.")
    @Schema(description = "가게 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    Long shopId
) {
}
