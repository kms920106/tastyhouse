package com.tastyhouse.webapi.product.adapter.in.web.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "상품 이미지 목록")
public record ProductImagesResponse(
    @Schema(description = "이미지 URL 목록", example = "[\"https://example.com/product1.jpg\", \"https://example.com/product2.jpg\"]")
    List<String> imageUrls
) {
    public static ProductImagesResponse from(List<String> imageUrls) {
        return new ProductImagesResponse(imageUrls);
    }
}
