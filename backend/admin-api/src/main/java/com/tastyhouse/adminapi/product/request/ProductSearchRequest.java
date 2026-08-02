package com.tastyhouse.adminapi.product.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "상품 검색 요청")
public record ProductSearchRequest(
    @Schema(description = "매장 ID", example = "1")
    Long shopId,

    @Schema(description = "카테고리 ID", example = "1")
    Long productCategoryId,

    @Schema(description = "상품명 (부분 일치 검색)", example = "불닭")
    String name,

    @Schema(description = "노출 여부", example = "true")
    Boolean visible,

    @Schema(description = "품절 여부", example = "false")
    Boolean soldOut
) {
}
