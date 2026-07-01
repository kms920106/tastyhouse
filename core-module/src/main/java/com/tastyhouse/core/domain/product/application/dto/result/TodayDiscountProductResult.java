package com.tastyhouse.core.domain.product.application.dto.result;

import java.math.BigDecimal;

import com.querydsl.core.annotations.QueryProjection;

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
