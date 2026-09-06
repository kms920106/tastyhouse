package com.tastyhouse.application.order.port.out;

import java.util.List;

public record OrderProductViewResult(
    Long orderProductId,
    Long productId,
    String name,
    String priceName,
    String imageUrl,
    Integer quantity,
    Integer originalPrice,
    Integer discountPrice,
    Integer totalOptionPrice,
    Integer totalPrice,
    List<OrderProductOptionResult> options,
    boolean reviewed
) {
}
