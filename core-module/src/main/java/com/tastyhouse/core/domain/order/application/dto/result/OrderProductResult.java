package com.tastyhouse.core.domain.order.application.dto.result;

import java.util.List;

public record OrderProductResult(
    Long id,
    Long productId,
    String name,
    String imageUrl,
    Integer quantity,
    Integer originalPrice,
    Integer discountPrice,
    Integer totalOptionPrice,
    Integer totalPrice,
    List<OrderProductOptionResult> options
) {
}
