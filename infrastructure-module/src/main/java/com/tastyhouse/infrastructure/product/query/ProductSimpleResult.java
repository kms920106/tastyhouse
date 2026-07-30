package com.tastyhouse.infrastructure.product.query;

import java.math.BigDecimal;

import com.querydsl.core.annotations.QueryProjection;

/**
 * 다른 도메인 화면(에디터 추천 카드 등)에 곁들여 노출하는 상품 요약 read model.
 */
public record ProductSimpleResult(
    Long id,
    String shopName,
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
