package com.tastyhouse.webapi.product.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "상품 이미지 목록")
public record ProductImagesResponse(
    @Schema(description = "이미지 URL 목록", example = "[\"https://example.com/product1.jpg\", \"https://example.com/product2.jpg\"]")
    List<String> imageUrls
) {
    public static ProductImagesResponse from(List<String> imageUrls) {
        return new ProductImagesResponse(imageUrls);
    }
}
