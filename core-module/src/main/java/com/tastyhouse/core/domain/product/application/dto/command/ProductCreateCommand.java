package com.tastyhouse.core.domain.product.application.dto.command;

import java.math.BigDecimal;

public record ProductCreateCommand(
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
) {

    public static ProductCreateCommand of(
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
    ) {
        return new ProductCreateCommand(
            shopId, productCategoryId, name, description, originalPrice, discountPrice, discountRate,
            rating, reviewCount, representative, spiciness, soldOut, visible, sort
        );
    }
}
