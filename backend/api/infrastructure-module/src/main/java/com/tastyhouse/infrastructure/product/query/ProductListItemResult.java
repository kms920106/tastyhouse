package com.tastyhouse.infrastructure.product.query;

import java.math.BigDecimal;

import com.querydsl.core.annotations.QueryProjection;

/**
 * 관리자 상품 목록 항목 read model.
 */
public record ProductListItemResult(
    Long id,
    String shopName,
    String name,
    Integer originalPrice,
    Integer discountPrice,
    BigDecimal discountRate,
    boolean representative,
    boolean soldOut,
    boolean visible,
    Integer sort
) {
    @QueryProjection
    public ProductListItemResult {
    }
}
