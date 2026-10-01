package com.tastyhouse.ceoapi.product.adapter.in.web.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.product.port.out.ProductOptionGroupMergeSuggestionResult;

@Schema(description = "합치기 추천 묶음에 속한 옵션그룹")
public record ProductOptionGroupMergeSuggestionGroupResponse(
    @Schema(description = "옵션그룹 ID", example = "10")
    Long id,

    @Schema(description = "이 그룹이 연결된 메뉴 수", example = "3")
    Integer linkedProductCount,

    @Schema(description = "이 그룹이 연결된 메뉴명 목록")
    List<String> linkedProductNames
) {

    public static ProductOptionGroupMergeSuggestionGroupResponse from(
        ProductOptionGroupMergeSuggestionResult.Group group
    ) {
        return new ProductOptionGroupMergeSuggestionGroupResponse(
            group.id(),
            group.linkedProductCount(),
            group.linkedProductNames()
        );
    }
}
