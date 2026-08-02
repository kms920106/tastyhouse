package com.tastyhouse.adminapi.product.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "상품 이미지 목록 응답")
public record ProductImagesResponse(
    @Schema(description = "이미지 접근 URL 목록")
    List<String> imageUrls
) {
    public static ProductImagesResponse from(List<String> imageUrls) {
        return new ProductImagesResponse(imageUrls);
    }
}
