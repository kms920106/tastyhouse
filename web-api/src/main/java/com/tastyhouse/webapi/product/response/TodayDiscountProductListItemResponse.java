package com.tastyhouse.webapi.product.response;

import java.math.BigDecimal;

public record TodayDiscountProductListItemResponse(
    Long id,
    String placeName,
    String name,
    String imageUrl,
    Integer originalPrice,
    Integer discountPrice,
    BigDecimal discountRate
) {
    public static TodayDiscountProductListItemResponse from(
    Long id,
    String placeName,
    String name,
    String imageUrl,
    Integer originalPrice,
    Integer discountPrice,
    BigDecimal discountRate
    ) {
    return new TodayDiscountProductListItemResponse(
        id,
        placeName,
        name,
        imageUrl,
        originalPrice,
        discountPrice,
        discountRate
    );
    }
}
