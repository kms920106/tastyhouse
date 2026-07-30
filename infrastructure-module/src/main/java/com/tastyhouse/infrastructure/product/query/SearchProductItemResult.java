package com.tastyhouse.infrastructure.product.query;

import java.math.BigDecimal;

import com.querydsl.core.annotations.QueryProjection;

/**
 * 통합검색 상품 항목 read model.
 */
public record SearchProductItemResult(
    Long id,
    String shopName,
    String name,
    String imageFilePath,
    Integer originalPrice,
    Integer discountPrice,
    BigDecimal discountRate,
    Double rating,
    Integer reviewCount,
    boolean representative,
    Integer spiciness
) {
    @QueryProjection
    public SearchProductItemResult {
    }
}
