package com.tastyhouse.ceoapi.product.adapter.in.web.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.product.port.out.ProductOptionGroupLinkedProductsResult;

@Schema(description = "옵션그룹별 연결 메뉴 목록")
public record ProductOptionGroupLinkedProductsResponse(
    @Schema(description = "옵션그룹 ID", example = "10")
    Long optionGroupId,

    @Schema(description = "이 그룹을 사용하는 메뉴 목록")
    List<ProductOptionGroupLinkedProductResponse> products
) {
    public static ProductOptionGroupLinkedProductsResponse from(ProductOptionGroupLinkedProductsResult result) {
        return new ProductOptionGroupLinkedProductsResponse(
            result.optionGroupId(),
            result.products().stream()
                .map(ProductOptionGroupLinkedProductResponse::from)
                .toList()
        );
    }
}
