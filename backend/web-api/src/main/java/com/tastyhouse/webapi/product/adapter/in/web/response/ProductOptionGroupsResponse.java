package com.tastyhouse.webapi.product.adapter.in.web.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.product.port.out.ProductOptionsResult;

@Schema(description = "상품 옵션 그룹 목록")
public record ProductOptionGroupsResponse(
    @Schema(description = "옵션 그룹 목록")
    List<ProductOptionGroupResponse> optionGroups
) {
    public static ProductOptionGroupsResponse from(ProductOptionsResult result) {
        return new ProductOptionGroupsResponse(
            result.optionGroups().stream().map(ProductOptionGroupResponse::from).toList()
        );
    }
}
