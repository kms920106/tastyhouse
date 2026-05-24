package com.tastyhouse.core.domain.product.application.dto.result;

import com.querydsl.core.annotations.QueryProjection;

import java.math.BigDecimal;

public record ProductSimpleResult(
    Long id,
    String placeName,
    String name,
    String imageUrl,
    Integer originalPrice,
    Integer discountPrice,
    BigDecimal discountRate
) {
    @QueryProjection
    public ProductSimpleResult {
    }
}
