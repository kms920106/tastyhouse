package com.tastyhouse.core.domain.shop.application.dto.result;

import java.util.List;

import com.querydsl.core.annotations.QueryProjection;

import com.tastyhouse.core.domain.product.application.dto.result.ProductSimpleResult;

public record EditorChoiceDto(
    Long id,
    Long shopId,
    String name,
    String title,
    String content,
    String shopImageUrl,
    List<ProductSimpleResult> products
) {
    @QueryProjection
    public EditorChoiceDto {
    }
}
