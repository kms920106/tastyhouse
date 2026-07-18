package com.tastyhouse.core.domain.product.application.dto;

public record ProductSearchCondition(
    Long shopId,
    Long productCategoryId,
    String name,
    Boolean visible,
    Boolean soldOut
) {

    public static ProductSearchCondition of(
        Long shopId,
        Long productCategoryId,
        String name,
        Boolean visible,
        Boolean soldOut
    ) {
        return new ProductSearchCondition(shopId, productCategoryId, name, visible, soldOut);
    }
}
