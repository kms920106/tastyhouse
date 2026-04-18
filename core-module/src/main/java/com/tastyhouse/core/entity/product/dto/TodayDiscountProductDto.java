package com.tastyhouse.core.entity.product.dto;

import com.querydsl.core.annotations.QueryProjection;

import java.math.BigDecimal;

public record TodayDiscountProductDto(
    Long id,
    String placeName,
    String name,
    String imageUrl,
    Integer originalPrice,
    Integer discountPrice,
    BigDecimal discountRate
) {
    @QueryProjection
    public TodayDiscountProductDto {
    }
}
