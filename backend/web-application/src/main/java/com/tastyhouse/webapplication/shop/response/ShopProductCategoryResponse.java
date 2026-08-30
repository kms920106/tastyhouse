package com.tastyhouse.webapplication.shop.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.webapplication.product.response.ProductSummaryResponse;

@Schema(description = "가게 상품 카테고리 응답")
public record ShopProductCategoryResponse(
    @Schema(description = "카테고리명", example = "대표 상품")
    String categoryName,

    @Schema(description = "상품 목록")
    List<ProductSummaryResponse> products
) {
    public static ShopProductCategoryResponse from(
        String categoryName,
        List<ProductSummaryResponse> menus
    ) {
        return new ShopProductCategoryResponse(
            categoryName,
            menus
        );
    }
}
