package com.tastyhouse.application.product.port.out;

import java.math.BigDecimal;

public record ProductListItemResult(
    Long id,
    String shopName,
    String name,
    Integer originalPrice,
    Integer discountPrice,
    BigDecimal discountRate,
    boolean representative,
    boolean soldOut,
    boolean visible,
    Integer sort
) {
}
