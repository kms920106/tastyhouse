package com.tastyhouse.webapplication.product.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "상품 배치 조회 응답")
public record ProductBatchResponse(
    @Schema(description = "상품 목록 (요청한 모든 상품을 요청 순서대로 포함. 판매 종료/미존재 상품은 available=false)")
    List<ProductResponse> products
) {
    public static ProductBatchResponse from(List<ProductResponse> products) {
        return new ProductBatchResponse(products);
    }
}
