package com.tastyhouse.application.product.port.out;

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
}
