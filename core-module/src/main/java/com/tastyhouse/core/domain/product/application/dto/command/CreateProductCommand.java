package com.tastyhouse.core.domain.product.application.dto.command;

import java.math.BigDecimal;

public record CreateProductCommand(
    Long placeId,
    Long productCategoryId,
    String name,
    String description,
    Integer originalPrice,
    Integer discountPrice,
    BigDecimal discountRate,
    Double rating,
    Integer reviewCount,
    Boolean isRepresentative,
    Integer spiciness,
    Boolean isSoldOut,
    Boolean isActive,
    Integer sort
) {}
