package com.tastyhouse.infrastructure.jpa.product.query;

import java.time.LocalDateTime;

public record ProductAvailabilityRow(
    Long categoryId,
    String categoryName,
    Integer categorySort,
    Long productId,
    String productName,
    Integer originalPrice,
    Integer discountPrice,
    String imageUrl,
    Boolean soldOut,
    LocalDateTime soldOutUntil,
    Boolean visible,
    Boolean representative,
    Integer linkSort
) {
}
