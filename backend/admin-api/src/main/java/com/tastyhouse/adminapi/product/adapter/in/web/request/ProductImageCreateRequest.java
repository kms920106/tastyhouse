package com.tastyhouse.adminapi.product.adapter.in.web.request;

import com.tastyhouse.adminapi.product.application.port.in.ProductImageCreateCommand;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "상품 이미지 등록 요청")
public record ProductImageCreateRequest(
    @NotNull(message = "이미지 파일 ID는 필수입니다.")
    @Schema(description = "사전 업로드된 이미지 파일 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    Long imageFileId,

    @NotNull(message = "정렬 순서는 필수입니다.")
    @Schema(description = "정렬 순서", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    Integer sort,

    @NotNull(message = "노출 여부는 필수입니다.")
    @Schema(description = "노출 여부", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
    Boolean visible
) {

    public ProductImageCreateCommand toCommand(Long productId) {
        return new ProductImageCreateCommand(productId, imageFileId, sort, visible);
    }
}
