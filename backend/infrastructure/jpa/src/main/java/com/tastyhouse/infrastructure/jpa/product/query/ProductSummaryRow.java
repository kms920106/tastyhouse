package com.tastyhouse.infrastructure.jpa.product.query;

import java.math.BigDecimal;

public record ProductSummaryRow(
    Long id,
    String name,
    Integer originalPrice,
    Integer discountPrice,
    BigDecimal discountRate
) {
}
