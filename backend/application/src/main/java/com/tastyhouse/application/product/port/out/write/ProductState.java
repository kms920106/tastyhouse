package com.tastyhouse.application.product.port.out.write;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ProductState(
    Long id,
    Long shopId,
    Long productCategoryId,
    String name,
    String description,
    Integer originalPrice,
    ProductDiscountInfoSnapshot discountInfo,
    Double rating,
    Integer reviewCount,
    boolean representative,
    Integer spiciness,
    boolean soldOut,
    LocalDateTime soldOutUntil,
    boolean visible,
    Integer sort,
    boolean ratingExcluded,
    boolean deleted,
    String composition,
    boolean singleServing,
    LocalDate exposureStartDate,
    LocalDate exposureEndDate,
    String vegetarianType,
    String weightText,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
