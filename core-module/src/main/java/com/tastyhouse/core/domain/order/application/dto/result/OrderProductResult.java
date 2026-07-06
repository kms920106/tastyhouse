package com.tastyhouse.core.domain.order.application.dto.result;

import java.util.List;

import com.tastyhouse.core.domain.order.domain.vo.OrderProductId;

public record OrderProductResult(
    OrderProductId orderProductId,
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
