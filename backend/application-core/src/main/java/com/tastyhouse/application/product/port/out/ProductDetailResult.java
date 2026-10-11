package com.tastyhouse.application.product.port.out;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProductDetailResult(
    Long id,
    Long shopId,
    Long productCategoryId,
    String name,
    String description,
    Integer originalPrice,
    Integer discountPrice,
    BigDecimal discountRate,
    Double rating,
    Integer reviewCount,
    boolean representative,
    Integer spiciness,
    boolean soldOut,
    boolean visible,
    Integer sort,
    String weightText,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
