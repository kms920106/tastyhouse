package com.tastyhouse.infrastructure.product.persistence;

import java.math.BigDecimal;

import jakarta.persistence.Embeddable;

@Embeddable
public record ProductDiscountInfoEmbeddable(Integer discountPrice, BigDecimal discountRate) {
}
