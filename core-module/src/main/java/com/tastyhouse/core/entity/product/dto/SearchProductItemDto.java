package com.tastyhouse.core.entity.product.dto;

import com.querydsl.core.annotations.QueryProjection;

import java.math.BigDecimal;

public record SearchProductItemDto(
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
    public SearchProductItemDto {
    }
}
