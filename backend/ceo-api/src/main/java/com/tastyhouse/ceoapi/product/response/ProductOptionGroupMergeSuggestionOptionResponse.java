package com.tastyhouse.ceoapi.product.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "합치기 추천 묶음의 대표 옵션")
public record ProductOptionGroupMergeSuggestionOptionResponse(
    @Schema(description = "옵션 ID", example = "100")
    Long id,

    @Schema(description = "옵션명", example = "곱빼기")
    String name,

    @Schema(description = "추가 금액(원)", example = "1000")
    Integer additionalPrice
) {

    public static ProductOptionGroupMergeSuggestionOptionResponse from(
        Long id,
        String name,
        Integer additionalPrice
    ) {
        return new ProductOptionGroupMergeSuggestionOptionResponse(
            id,
            name,
            additionalPrice
        );
    }
}
