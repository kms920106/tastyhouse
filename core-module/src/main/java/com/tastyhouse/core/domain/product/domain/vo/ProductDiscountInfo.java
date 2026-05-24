package com.tastyhouse.core.domain.product.domain.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.math.BigDecimal;
import java.util.Objects;

@Embeddable
public final class ProductDiscountInfo {

    @Column(name = "discount_price")
    private Integer discountPrice;

    @Column(name = "discount_rate")
    private BigDecimal discountRate;

    protected ProductDiscountInfo() {}

    private ProductDiscountInfo(Integer discountPrice, BigDecimal discountRate) {
        this.discountPrice = discountPrice;
        this.discountRate = discountRate;
    }

    public static ProductDiscountInfo of(Integer discountPrice, BigDecimal discountRate) {
        return new ProductDiscountInfo(discountPrice, discountRate);
    }

    public static ProductDiscountInfo none() {
        return new ProductDiscountInfo(null, null);
    }

    public Integer discountPrice() {
        return discountPrice;
    }

    public BigDecimal discountRate() {
        return discountRate;
    }

    public boolean hasDiscount() {
        return discountPrice != null;
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
