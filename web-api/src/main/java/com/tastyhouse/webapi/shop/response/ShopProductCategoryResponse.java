package com.tastyhouse.webapi.shop.response;

import com.tastyhouse.webapi.product.response.ProductSummaryResponse;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

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
