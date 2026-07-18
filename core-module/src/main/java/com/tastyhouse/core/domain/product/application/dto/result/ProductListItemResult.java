package com.tastyhouse.core.domain.product.application.dto.result;

import java.math.BigDecimal;

import com.querydsl.core.annotations.QueryProjection;

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
