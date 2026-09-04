package com.tastyhouse.application.product.port.out;

import java.math.BigDecimal;

/**
 * 오늘의 할인 상품 목록 항목 read model.
 */
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
