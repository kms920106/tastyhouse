package com.tastyhouse.ceoapi.product.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "옵션 등록 요청")
public record ProductOptionCreateRequest(
    @NotNull(message = "가게 ID는 필수입니다.")
    @Schema(description = "대상 가게 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    Long shopId,

    @NotBlank(message = "옵션명은 필수입니다.")
    @Size(max = 100, message = "옵션명은 100자 이하여야 합니다.")
    @Schema(description = "옵션명", example = "아주 매운맛", requiredMode = Schema.RequiredMode.REQUIRED)
    String name,

    @NotNull(message = "추가 금액은 필수입니다.")
    @Min(value = 0, message = "추가 금액은 0 이상이어야 합니다.")
    @Schema(description = "추가 금액(원). 무료 옵션은 0", example = "500",
        requiredMode = Schema.RequiredMode.REQUIRED)
    Integer additionalPrice
) {
}
