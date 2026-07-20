package com.tastyhouse.core.domain.product.domain.vo;

import java.math.BigDecimal;
import java.util.Objects;

public final class ProductDiscountInfo {

    private final Integer discountPrice;

    private final BigDecimal discountRate;

    private ProductDiscountInfo(Integer discountPrice, BigDecimal discountRate) {
        this.discountPrice = discountPrice;
        this.discountRate = discountRate;
    }

    public static ProductDiscountInfo of(Integer discountPrice, BigDecimal discountRate) {
        return new ProductDiscountInfo(discountPrice, discountRate);
    }

    public Integer discountPrice() {
        return discountPrice;
    }

    public BigDecimal discountRate() {
        return discountRate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProductDiscountInfo that)) return false;
        return Objects.equals(discountPrice, that.discountPrice)
            && Objects.equals(discountRate, that.discountRate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(discountPrice, discountRate);
    }
}
