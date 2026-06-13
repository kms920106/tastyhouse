package com.tastyhouse.core.domain.order.application.dto.result;

import java.util.List;

public record OrderItemResult(
    Long id,
    Long productId,
    String productName,
    String productImageFilePath,
    Integer quantity,
    Integer unitPrice,
    Integer discountPrice,
    Integer optionTotalPrice,
    Integer totalPrice,
    List<OrderItemOptionResult> options
) {
}
