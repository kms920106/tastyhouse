package com.tastyhouse.core.domain.product.domain.vo;

import java.math.BigDecimal;

public record ProductDiscountInfo(Integer discountPrice, BigDecimal discountRate) {

    public static ProductDiscountInfo of(Integer discountPrice, BigDecimal discountRate) {
        return new ProductDiscountInfo(discountPrice, discountRate);
    }
}
