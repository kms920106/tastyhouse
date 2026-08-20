package com.tastyhouse.infrastructure.product.query;

import com.querydsl.core.annotations.QueryProjection;

/**
 * 상품 카테고리 read model.
 */
public record ProductCategoryResult(
    Long id,
    Long shopId,
    String name,
    String description,
    Integer sort,
    boolean visible
) {
    @QueryProjection
    public ProductCategoryResult {
    }
}
