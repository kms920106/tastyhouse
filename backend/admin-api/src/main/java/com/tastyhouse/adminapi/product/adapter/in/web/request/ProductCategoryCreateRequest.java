package com.tastyhouse.adminapi.product.adapter.in.web.request;

import com.tastyhouse.adminapi.product.application.port.in.ProductCategoryCreateCommand;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "상품 카테고리 생성 요청")
public record ProductCategoryCreateRequest(
    @NotNull(message = "매장 ID는 필수입니다.")
    @Schema(description = "매장 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    Long shopId,

    @NotBlank(message = "카테고리명은 필수입니다.")
    @Schema(description = "카테고리명", example = "면류", requiredMode = Schema.RequiredMode.REQUIRED)
    String name,

    @NotNull(message = "정렬 순서는 필수입니다.")
    @Schema(description = "정렬 순서", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    Integer sort,

    @NotNull(message = "노출 여부는 필수입니다.")
    @Schema(description = "노출 여부", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
    Boolean visible
) {

    public ProductCategoryCreateCommand toCommand() {
        return new ProductCategoryCreateCommand(shopId, name, sort, visible);
    }
}
