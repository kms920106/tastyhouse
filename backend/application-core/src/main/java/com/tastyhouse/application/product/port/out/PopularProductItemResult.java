package com.tastyhouse.application.product.port.out;

import java.math.BigDecimal;

public record PopularProductItemResult(
    Long id,
    String name,
    String imageUrl,
    Integer originalPrice,
    Integer discountPrice,
    BigDecimal discountRate,
    Double rating,
    Integer reviewCount,
    boolean representative,
    Integer spiciness,
    long salesQuantity
) {
}
