package com.tastyhouse.infrastructure.persistence.product.persistence;

import java.math.BigDecimal;

import jakarta.persistence.Embeddable;

@Embeddable
public record ProductDiscountInfoEmbeddable(Integer discountPrice, BigDecimal discountRate) {
}
