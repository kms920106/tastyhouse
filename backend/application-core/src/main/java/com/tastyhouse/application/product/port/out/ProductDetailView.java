package com.tastyhouse.application.product.port.out;

import java.math.BigDecimal;
import java.util.List;

public record ProductDetailView(
    Long id,
    String name,
    String description,
    Integer originalPrice,
    Integer discountPrice,
    BigDecimal discountRate,
    boolean soldOut,
    String weightText,
    long menuReviewCount,
    List<ProductPriceView> prices
) {
}
