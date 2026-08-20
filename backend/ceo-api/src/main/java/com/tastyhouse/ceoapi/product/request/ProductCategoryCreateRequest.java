package com.tastyhouse.ceoapi.product.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "메뉴그룹 등록 요청")
public record ProductCategoryCreateRequest(
    @NotNull(message = "가게 ID는 필수입니다.")
    @Schema(description = "대상 가게 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    Long shopId,

    @NotBlank(message = "메뉴그룹명은 필수입니다.")
    @Size(max = 100, message = "메뉴그룹명은 100자 이하여야 합니다.")
    @Schema(description = "메뉴그룹명", example = "인기 메뉴", requiredMode = Schema.RequiredMode.REQUIRED)
    String name,

    @Size(max = 500, message = "메뉴그룹 설명은 500자 이하여야 합니다.")
    @Schema(description = "메뉴그룹 설명", example = "사장님이 가장 추천하는 메뉴들입니다.")
    String description
) {
}
