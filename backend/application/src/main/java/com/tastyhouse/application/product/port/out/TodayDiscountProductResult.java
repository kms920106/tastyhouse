package com.tastyhouse.application.product.port.out;

import java.math.BigDecimal;

public record TodayDiscountProductResult(
    Long id,
    String shopName,
    String name,
    String imageUrl,
    Integer originalPrice,
    Integer discountPrice,
    BigDecimal discountRate
) {
}
