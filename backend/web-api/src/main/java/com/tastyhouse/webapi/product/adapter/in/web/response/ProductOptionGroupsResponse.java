package com.tastyhouse.webapi.product.adapter.in.web.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "상품 옵션 그룹 목록")
public record ProductOptionGroupsResponse(
    @Schema(description = "옵션 그룹 목록")
    List<ProductOptionGroupResponse> optionGroups
) {
    public static ProductOptionGroupsResponse from(List<ProductOptionGroupResponse> optionGroups) {
        return new ProductOptionGroupsResponse(optionGroups);
    }
}
