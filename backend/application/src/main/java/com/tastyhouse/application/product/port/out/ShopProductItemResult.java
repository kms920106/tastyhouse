package com.tastyhouse.application.product.port.out;

import java.math.BigDecimal;

public record ShopProductItemResult(
    Long id,
    Long productCategoryId,
    String name,
    String imageUrl,
    Integer originalPrice,
    Integer discountPrice,
    BigDecimal discountRate,
    Double rating,
    Integer reviewCount,
    boolean representative,
    Integer spiciness,
    boolean soldOut
) {
}
