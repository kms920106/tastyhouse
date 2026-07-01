package com.tastyhouse.webapi.product.response;

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

    @Schema(description = "배치 조회 상품")
    public record ProductResponse(
        @Schema(description = "상품 ID", example = "1")
        Long id,

        @Schema(description = "구매 가능 여부. 판매 종료/미존재 상품이면 false 이고 이하 필드는 비어 있음", example = "true")
        boolean available,

        @Schema(description = "상품명 (available=false 면 null)", example = "후라이드 치킨", nullable = true)
        String name,

        @Schema(description = "상품 대표 이미지 URL (available=false 이거나 이미지가 없으면 null)", example = "https://cdn.example.com/products/1.jpg", nullable = true)
        String imageUrl,

        @Schema(description = "정가 (available=false 면 null)", example = "18000", nullable = true)
        Integer originalPrice,

        @Schema(description = "할인가. 할인이 없거나 available=false 면 null", example = "16000", nullable = true)
        Integer discountPrice,

        @Schema(description = "요청한 옵션 중 조회에 성공한 옵션 목록 (available=false 면 빈 배열)")
        List<OptionResponse> options
    ) {
    }

    @Schema(description = "배치 조회 옵션")
    public record OptionResponse(
        @Schema(description = "옵션 ID", example = "1")
        Long id,

        @Schema(description = "옵션명", example = "라지")
        String name,

        @Schema(description = "옵션 추가 금액", example = "3000")
        Integer price
    ) {
    }
}
