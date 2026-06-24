package com.tastyhouse.core.domain.product.application.dto.command;

import java.math.BigDecimal;

public record CreateProductCommand(
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
    Integer sort
) {}
