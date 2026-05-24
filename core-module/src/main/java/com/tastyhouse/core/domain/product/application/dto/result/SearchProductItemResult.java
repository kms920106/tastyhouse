package com.tastyhouse.core.domain.product.application.dto.result;

import com.querydsl.core.annotations.QueryProjection;

import java.math.BigDecimal;

public record SearchProductItemResult(
    Long id,
    String placeName,
    String name,
    String imageFilePath,
    Integer originalPrice,
    Integer discountPrice,
    BigDecimal discountRate,
    Double rating,
    Integer reviewCount,
    Boolean isRepresentative,
    Integer spiciness
) {
    @QueryProjection
    public SearchProductItemResult {
    }
}
