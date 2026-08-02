package com.tastyhouse.infrastructure.shop.query;

import java.util.List;

import com.querydsl.core.annotations.QueryProjection;

import com.tastyhouse.infrastructure.product.query.ProductSimpleResult;

public record EditorChoiceResult(
    Long id,
    Long shopId,
    String name,
    String title,
    String content,
    String shopImageUrl,
    List<ProductSimpleResult> products
) {
    @QueryProjection
    public EditorChoiceResult {
    }
}
