package com.tastyhouse.application.product.port.out;

import java.time.LocalDateTime;

public record ProductAvailabilityItemResult(
    Long categoryId,
    String categoryName,
    Integer categorySort,
    Long id,
    String name,
    Integer originalPrice,
    Integer discountPrice,
    String imageUrl,
    boolean soldOut,
    LocalDateTime soldOutUntil,
    boolean visible,
    boolean representative,
    Integer sort
) {
}
