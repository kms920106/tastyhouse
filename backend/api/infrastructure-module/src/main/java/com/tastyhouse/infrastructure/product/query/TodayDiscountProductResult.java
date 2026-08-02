package com.tastyhouse.infrastructure.product.query;

import java.math.BigDecimal;

import com.querydsl.core.annotations.QueryProjection;

/**
 * 오늘의 할인 상품 목록 항목 read model.
 */
public record TodayDiscountProductResult(
    Long id,
    String shopName,
    String name,
    String imageUrl,
    Integer originalPrice,
    Integer discountPrice,
    BigDecimal discountRate
) {
    @QueryProjection
    public TodayDiscountProductResult {
    }
}
