package com.tastyhouse.webapi.product.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "상품 옵션 그룹 목록")
public record ProductOptionGroupsResponse(
    @Schema(description = "옵션 그룹 목록")
    List<OptionGroupResponse> optionGroups
) {
    public static ProductOptionGroupsResponse from(List<OptionGroupResponse> optionGroups) {
        return new ProductOptionGroupsResponse(optionGroups);
    }
}
