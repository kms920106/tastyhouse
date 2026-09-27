package com.tastyhouse.application.product.port.out.write;

import java.math.BigDecimal;

public record ProductDiscountInfoSnapshot(
    Integer discountPrice,
    BigDecimal discountRate
) {
}
