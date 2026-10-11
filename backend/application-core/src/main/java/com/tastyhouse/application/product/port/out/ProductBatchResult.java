package com.tastyhouse.application.product.port.out;

import java.math.BigDecimal;
import java.util.List;

public record ProductBatchResult(
    Long id,
    boolean available,
    String name,
    String imageUrl,
    Integer originalPrice,
    Integer discountPrice,
    BigDecimal discountRate,
    List<BatchOptionResult> options
) {
}
