package com.tastyhouse.core.domain.product.application.dto.command;

import java.math.BigDecimal;

public record ProductUpdateCommand(
    Long productCategoryId,
    String name,
    String description,
    Integer originalPrice,
    Integer discountPrice,
    BigDecimal discountRate,
    boolean representative,
    Integer spiciness,
    boolean soldOut,
    boolean visible,
    Integer sort
) {

    public static ProductUpdateCommand of(
        Long productCategoryId,
        String name,
        String description,
        Integer originalPrice,
        Integer discountPrice,
        BigDecimal discountRate,
        boolean representative,
        Integer spiciness,
        boolean soldOut,
        boolean visible,
        Integer sort
    ) {
        return new ProductUpdateCommand(
            productCategoryId,
            name,
            description,
            originalPrice,
            discountPrice,
            discountRate,
            representative,
            spiciness,
            soldOut,
            visible,
            sort
        );
    }
}
