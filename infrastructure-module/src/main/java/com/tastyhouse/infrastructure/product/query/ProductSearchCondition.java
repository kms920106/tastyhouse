package com.tastyhouse.infrastructure.product.query;

/**
 * 관리자 상품 목록 검색 조건. 각 필드가 null이면 해당 조건을 적용하지 않는다.
 */
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
