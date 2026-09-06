package com.tastyhouse.application.product.port.out;

import com.tastyhouse.domain.product.model.VegetarianType;

public record ProductManagementDetailResult(
    Long id,
    Long shopId,
    Long productCategoryId,
    String productCategoryName,
    String name,
    String composition,
    String description,
    Integer originalPrice,
    Integer discountPrice,
    boolean singleServing,
    Integer spiciness,
    boolean representative,
    boolean ratingExcluded,
    boolean soldOut,
    boolean visible,
    String imageUrl,
    VegetarianType vegetarianType,
    String weightText,
    boolean exposureScheduled
) {
}
